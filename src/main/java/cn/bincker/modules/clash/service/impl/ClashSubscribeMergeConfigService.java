package cn.bincker.modules.clash.service.impl;

import cn.bincker.modules.clash.dto.ClashSubscribeMergeConfigDto;
import cn.bincker.modules.clash.entity.ClashSubscribe;
import cn.bincker.modules.clash.entity.ClashSubscribeMergeConfig;
import cn.bincker.modules.clash.entity.config.ClashConfig;
import cn.bincker.modules.clash.entity.config.ProxyConfig;
import cn.bincker.modules.clash.entity.config.ProxyGroupConfig;
import cn.bincker.modules.clash.mapper.ClashSubscribeMapper;
import cn.bincker.modules.clash.mapper.ClashSubscribeMergeConfigMapper;
import cn.bincker.modules.clash.service.IClashSubscribeMergeConfigService;
import cn.bincker.modules.clash.utils.ProxyUrlParser;
import cn.bincker.modules.clash.vo.ClashSubscribeMergeConfigDetailVo;
import cn.bincker.modules.clash.vo.ClashSubscribeMergeConfigVo;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ClashSubscribeMergeConfigService implements IClashSubscribeMergeConfigService {
    private final ClashSubscribeMergeConfigMapper clashSubscribeMergeConfigMapper;
    private final ClashSubscribeMapper clashSubscribeMapper;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final ObjectMapper yamlMapper;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public ClashSubscribeMergeConfigService(ClashSubscribeMergeConfigMapper clashSubscribeMergeConfigMapper,
                                            ClashSubscribeMapper clashSubscribeMapper,
                                            ThreadPoolTaskExecutor taskExecutor) {
        this.clashSubscribeMergeConfigMapper = clashSubscribeMergeConfigMapper;
        this.clashSubscribeMapper = clashSubscribeMapper;
        this.taskExecutor = taskExecutor;
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    @PostConstruct
    public void startMergeTask() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> taskExecutor.execute(this::mergeTimeoutConfigs), 10, 60, TimeUnit.SECONDS);
    }

    private void mergeTimeoutConfigs() {
        List<ClashSubscribeMergeConfig> configs = clashSubscribeMergeConfigMapper.selectList(null);
        Date now = new Date();
        for (ClashSubscribeMergeConfig config : configs) {
            if (config.getIntervalMinutes() == null || config.getIntervalMinutes() <= 0) continue;
            Date last = config.getLatestMergeTime();
            if (last != null && now.getTime() - last.getTime() < config.getIntervalMinutes() * 60_000L) continue;
            try {
                mergeConfig(config);
            } catch (Exception e) {
                log.error("merge clash config error.", e);
            }
        }
    }

    private void mergeConfig(ClashSubscribeMergeConfig mergeConfig) throws IOException {
        var subscribeIds = mergeConfig.getSubscribeIds();
        if (subscribeIds == null || subscribeIds.isEmpty()) {
            log.warn("merge clash subscribe ids is empty. id={}", mergeConfig.getId());
            return;
        }
        List<ClashSubscribe> clashSubscribes;
        if (subscribeIds.equals("all")){
            clashSubscribes = clashSubscribeMapper.selectList(null);
        }else{
            clashSubscribes = clashSubscribeMapper.selectByIds(Arrays.stream(subscribeIds.split(",")).map(Long::valueOf).toList());
        }
        List<ProxyConfig> allProxies = new ArrayList<>();
        for (var subscribe : clashSubscribes) {
            if (subscribe == null || subscribe.getUrl() == null) continue;
            try {
                allProxies.addAll(Objects.requireNonNull(readSubscribe(subscribe)));
                subscribe.setStatus(ClashSubscribe.Status.NORMAL);
                subscribe.setLastUpdateTime(new Date());
            } catch (Exception e) {
                log.error("merge clash subscribe error. id={}", subscribe.getId(), e);
                subscribe.setStatus(ClashSubscribe.Status.ABNORMAL);
            }
            clashSubscribeMapper.updateById(subscribe);
        }
        // 筛选proxies
        List<ProxyConfig> filtered = allProxies;
        if (mergeConfig.getTypeRegex() != null && !mergeConfig.getTypeRegex().isEmpty()) {
            Pattern typePattern = Pattern.compile(mergeConfig.getTypeRegex());
            filtered = filtered.stream().filter(p -> p.getType() != null && typePattern.matcher(p.getType()).find()).toList();
        }
        if (mergeConfig.getNameRegex() != null && !mergeConfig.getNameRegex().isEmpty()) {
            Pattern namePattern = Pattern.compile(mergeConfig.getNameRegex());
            filtered = filtered.stream().filter(p -> p.getName() != null && namePattern.matcher(p.getName()).find()).toList();
        }
        // 构造新的ClashConfig
        ClashConfig override = mergeConfig.getOverrideConfig() != null && !mergeConfig.getOverrideConfig().isEmpty()
                ? yamlMapper.readValue(mergeConfig.getOverrideConfig(), ClashConfig.class)
                : new ClashConfig();
        override.setProxies(filtered);
        // proxyGroups
        if (override.getProxyGroups() == null || override.getProxyGroups().isEmpty()) {
            override.setProxyGroups(defaultProxyGroups(filtered));
        }else{
            var proxyGroups = override.getProxyGroups();
            var proxyNames = filtered.stream().map(ProxyConfig::getName).toList();
            proxyGroups.get(0).setProxies(
                    Stream.concat(
                            proxyGroups.stream().skip(1L).map(ProxyGroupConfig::getName),
                            proxyNames.stream()
                    ).toList()
            );
            proxyGroups.stream().skip(1).forEach(group->group.setProxies(proxyNames));
        }
        if (override.getRules() == null || override.getRules().isEmpty()) {
            override.setRules(defaultRules());
        }
        // 序列化回YAML
        String newYaml = yamlMapper.writeValueAsString(override);
        mergeConfig.setOverrideConfig(newYaml);
        mergeConfig.setLatestMergeTime(new Date());
        clashSubscribeMergeConfigMapper.updateById(mergeConfig);
    }

    private List<String> defaultRules() {
        return List.of(
                "DOMAIN-SUFFIX,local,DIRECT",
                "IP-CIDR,127.0.0.0/8,DIRECT",
                "IP-CIDR,172.16.0.0/12,DIRECT",
                "IP-CIDR,192.168.0.0/16,DIRECT",
                "IP-CIDR,10.0.0.0/8,DIRECT",
                "IP-CIDR,17.0.0.0/8,DIRECT",
                "IP-CIDR,100.64.0.0/10,DIRECT",
                "IP-CIDR,224.0.0.0/4,DIRECT",
                "IP-CIDR6,fe80::/10,DIRECT",
                "DOMAIN-SUFFIX,cn,DIRECT",
                "DOMAIN-KEYWORD,-cn,DIRECT",
                "GEOIP,CN,DIRECT",
                "MATCH,Default"
        );
    }

    private List<ProxyGroupConfig> defaultProxyGroups(List<ProxyConfig> proxies) {
        List<String> proxyNames = proxies.stream().map(ProxyConfig::getName).collect(Collectors.toList());
        List<ProxyGroupConfig> groups = new ArrayList<>();
        ProxyGroupConfig select = new ProxyGroupConfig();
        select.setName("Proxy");
        select.setType("select");
        select.setProxies(proxyNames);
        groups.add(select);
        ProxyGroupConfig auto = new ProxyGroupConfig();
        auto.setName("Auto");
        auto.setType("url-test");
        auto.setProxies(proxyNames);
        auto.setUrl("https://www.gstatic.com/generate_204");
        auto.setInterval(300);
        groups.add(auto);
        ProxyGroupConfig fallback = new ProxyGroupConfig();
        fallback.setName("Fallback");
        fallback.setType("fallback");
        fallback.setProxies(proxyNames);
        fallback.setUrl("https://www.gstatic.com/generate_204");
        fallback.setInterval(7200);
        groups.add(fallback);
        return groups;
    }

    private List<ProxyConfig> readSubscribe(ClashSubscribe subscribe) throws IOException, InterruptedException {
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(subscribe.getUrl()))
                .header("User-Agent", "Clash-Verge: v2.2.3")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        var content = response.body();
        response.headers().firstValue("subscription-userinfo").ifPresent(userinfo->{
            var params = Stream.of(userinfo.split(";"))
                    .map(item->{
                        var keyValue = item.split("=");
                        return Map.entry(keyValue[0].trim(), Long.parseLong(keyValue[1].trim()));
                    })
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            Optional.ofNullable(params.get("upload")).ifPresent(subscribe::setUploadTraffic);
            Optional.ofNullable(params.get("download")).ifPresent(subscribe::setDownloadTraffic);
            Optional.ofNullable(params.get("total")).ifPresent(subscribe::setTotalTraffic);
            Optional.ofNullable(params.get("expire")).ifPresent(expire->subscribe.setExpiredTime(new Date(expire * 1000L)));
        });
        if (content == null) return null;
        if (content.matches("^([a-zA-Z0-9/=]{4})+$")){
            return parseBase64Content(subscribe, content);
        }
        ClashConfig config = yamlMapper.readValue(content, ClashConfig.class);
        var proxies = config.getProxies();
        if (proxies == null || proxies.isEmpty()) return Collections.emptyList();
        if (subscribe.getSkipProxies() != null && subscribe.getSkipProxies() < proxies.size()) {
            proxies = proxies.subList(subscribe.getSkipProxies(), proxies.size());
        }
        return proxies;
    }

    private List<ProxyConfig> parseBase64Content(ClashSubscribe subscribe, String content) {
        content = new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
        return Stream.of(content.split("\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(line->{
                    try {
                        return ProxyUrlParser.parseUri(line);
                    }catch (Exception e){
                        log.error("parse url fail. content={}", line, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .skip(subscribe.getSkipProxies() == null ? 0 : subscribe.getSkipProxies())
                .toList();
    }

    @Override
    public Page<ClashSubscribeMergeConfigVo> page(ClashSubscribeMergeConfigDto dto, Page<ClashSubscribeMergeConfig> page) {
        var result = clashSubscribeMergeConfigMapper.selectPage(page, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ClashSubscribeMergeConfig>()
                .like(dto.getName() != null, "name", dto.getName())
        );
        var voPage = new Page<ClashSubscribeMergeConfigVo>(result.getCurrent(), result.getSize(), result.getTotal());
        Map<Long, ClashSubscribe> subscribeMap;
        if (result.getRecords().stream().anyMatch(item->"all".equalsIgnoreCase(item.getSubscribeIds()))){
            subscribeMap = clashSubscribeMapper.selectList(Wrappers.emptyWrapper())
                    .stream()
                    .collect(Collectors.toMap(ClashSubscribe::getId, a->a));
        }else{
            subscribeMap = clashSubscribeMapper.selectByIds(
                            result.getRecords().stream().map(item->Stream.of(item.getSubscribeIds().split(",")).map(Long::valueOf).toList())
                                    .reduce(new ArrayList<Long>(), (a,b)->{
                                        a.addAll(b);
                                        return a;
                                    })
                    )
                    .stream()
                    .collect(Collectors.toMap(ClashSubscribe::getId, a->a));
        }
        voPage.setRecords(
                result.getRecords().stream()
                        .map(item->{
                            var vo = new ClashSubscribeMergeConfigVo();
                            BeanUtils.copyProperties(item, vo);
                            Collection<ClashSubscribe> subscribes;
                            if ("all".equalsIgnoreCase(item.getSubscribeIds())){
                                subscribes = subscribeMap.values();
                            }else{
                                subscribes = Stream.of(item.getSubscribeIds().split(",")).map(subscribeMap::get)
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());
                            }
                            vo.setDownloadTraffic(subscribes.stream().mapToLong(ClashSubscribe::getDownloadTraffic).sum());
                            vo.setUploadTraffic(subscribes.stream().mapToLong(ClashSubscribe::getUploadTraffic).sum());
                            vo.setTotalTraffic(subscribes.stream().mapToLong(ClashSubscribe::getTotalTraffic).sum());
                            return vo;
                        })
                        .toList()
        );
        return voPage;
    }

    @Override
    public ClashSubscribeMergeConfig getByToken(String token) {
        return clashSubscribeMergeConfigMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ClashSubscribeMergeConfig>().eq("token", token));
    }

    @Override
    public ClashSubscribeMergeConfig add(ClashSubscribeMergeConfigDto dto) {
        ClashSubscribeMergeConfig entity = new ClashSubscribeMergeConfig();
        BeanUtils.copyProperties(dto, entity);
        // token 需要生成，简单用UUID，实际可自定义
        entity.setToken(java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        clashSubscribeMergeConfigMapper.insert(entity);
        return entity;
    }

    @Override
    public ClashSubscribeMergeConfig update(ClashSubscribeMergeConfigDto dto) {
        Assert.notNull(dto, "参数不能为空");
        Assert.notNull(dto.getName(), "名称不能为空");
        ClashSubscribeMergeConfig entity = clashSubscribeMergeConfigMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ClashSubscribeMergeConfig>().eq("name", dto.getName()));
        if (entity == null) throw new IllegalArgumentException("未找到对应合并配置");
        BeanUtils.copyProperties(dto, entity);
        clashSubscribeMergeConfigMapper.updateById(entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        clashSubscribeMergeConfigMapper.deleteById(id);
    }

    @Override
    public ClashSubscribeMergeConfigDetailVo getDetailById(Long id) {
        var target = clashSubscribeMergeConfigMapper.selectById(id);
        var vo = new ClashSubscribeMergeConfigDetailVo();
        BeanUtils.copyProperties(target, vo);
        List<ClashSubscribe> subscribes;
        if ("all".equalsIgnoreCase(target.getSubscribeIds())) {
            subscribes = clashSubscribeMapper.selectList(Wrappers.emptyWrapper());
        }else{
            subscribes = clashSubscribeMapper.selectByIds(
                    Stream.of(target.getSubscribeIds().split(",")).map(Long::valueOf).toList()
            );
        }
        if (subscribes.size() > 3) {
            vo.setSubscribeNames(subscribes.subList(0, 3).stream().map(ClashSubscribe::getName).collect(Collectors.joining("、")) + "等" + subscribes.size() + "个订阅");
        }else{
            vo.setSubscribeNames(subscribes.stream().map(ClashSubscribe::getName).collect(Collectors.joining("、")));
        }
        return null;
    }
}