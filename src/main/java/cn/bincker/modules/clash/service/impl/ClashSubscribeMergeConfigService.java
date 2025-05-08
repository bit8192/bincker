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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
            String yaml = readUrlContent(subscribe.getUrl());
            ClashConfig config = yamlMapper.readValue(yaml, ClashConfig.class);
            if (config.getProxies() != null) allProxies.addAll(config.getProxies());
        }
        // 筛选proxies
        List<ProxyConfig> filtered = allProxies;
        if (mergeConfig.getTypeRegex() != null && !mergeConfig.getTypeRegex().isEmpty()) {
            Pattern typePattern = Pattern.compile(mergeConfig.getTypeRegex());
            filtered = filtered.stream().filter(p -> p.getType() != null && typePattern.matcher(p.getType()).find()).collect(Collectors.toList());
        }
        if (mergeConfig.getNameRegex() != null && !mergeConfig.getNameRegex().isEmpty()) {
            Pattern namePattern = Pattern.compile(mergeConfig.getNameRegex());
            filtered = filtered.stream().filter(p -> p.getName() != null && namePattern.matcher(p.getName()).find()).collect(Collectors.toList());
        }
        // 构造新的ClashConfig
        ClashConfig override = mergeConfig.getOverrideConfig() != null && !mergeConfig.getOverrideConfig().isEmpty()
                ? yamlMapper.readValue(mergeConfig.getOverrideConfig(), ClashConfig.class)
                : new ClashConfig();
        override.setProxies(filtered);
        // proxyGroups
        if (override.getProxyGroups() == null || override.getProxyGroups().isEmpty()) {
            override.setProxyGroups(defaultProxyGroups(filtered));
        }
        // 序列化回YAML
        String newYaml = yamlMapper.writeValueAsString(override);
        mergeConfig.setOverrideConfig(newYaml);
        mergeConfig.setLatestMergeTime(new Date());
        clashSubscribeMergeConfigMapper.updateById(mergeConfig);
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
        auto.setUrl("http://www.gstatic.com/generate_204");
        auto.setInterval(300);
        groups.add(auto);
        return groups;
    }

    private String readUrlContent(String url) throws IOException {
        try (java.util.Scanner scanner = new java.util.Scanner(new URL(url).openStream(), StandardCharsets.UTF_8)) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    @Override
    public Page<ClashSubscribeMergeConfig> page(ClashSubscribeMergeConfigDto dto, Page<ClashSubscribeMergeConfig> page) {
        return clashSubscribeMergeConfigMapper.selectPage(page, new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ClashSubscribeMergeConfig>()
                .like(dto.getName() != null, "name", dto.getName())
        );
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
} 