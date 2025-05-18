package cn.bincker.modules.clash.service.impl;

import cn.bincker.modules.clash.dto.ClashSubscribeMergeConfigDto;
import cn.bincker.modules.clash.entity.ClashSubscribe;
import cn.bincker.modules.clash.entity.ClashSubscribeMergeConfig;
import cn.bincker.modules.clash.entity.config.ClashConfig;
import cn.bincker.modules.clash.entity.config.ProxyConfig;
import cn.bincker.modules.clash.entity.config.ProxyGroupConfig;
import cn.bincker.modules.clash.handler.ClashConfigTypeHandler;
import cn.bincker.modules.clash.mapper.ClashSubscribeMapper;
import cn.bincker.modules.clash.mapper.ClashSubscribeMergeConfigMapper;
import cn.bincker.modules.clash.service.IClashSubscribeMergeConfigService;
import cn.bincker.modules.clash.utils.ProxyUrlParser;
import cn.bincker.modules.clash.vo.ClashSubscribeMergeConfigDetailVo;
import cn.bincker.modules.clash.vo.ClashSubscribeMergeConfigVo;
import cn.bincker.modules.clash.vo.ClashYamlContentVo;
import cn.bincker.modules.clash.vo.GithubReleasesInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private final Yaml yaml;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private boolean mihomoInstalled;
    private String mihomoPath;
    private final ObjectMapper objectMapper;

    public ClashSubscribeMergeConfigService(
            ClashSubscribeMergeConfigMapper clashSubscribeMergeConfigMapper,
            ClashSubscribeMapper clashSubscribeMapper,
            ThreadPoolTaskExecutor taskExecutor,
            ObjectMapper objectMapper
    ) {
        this.clashSubscribeMergeConfigMapper = clashSubscribeMergeConfigMapper;
        this.clashSubscribeMapper = clashSubscribeMapper;
        this.taskExecutor = taskExecutor;
        this.objectMapper = objectMapper;

        yaml = ClashConfigTypeHandler.getClashConfigYaml();
    }

    @PostConstruct
    public void startMergeTask() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> taskExecutor.execute(this::mergeTimeoutConfigs), 10, 60, TimeUnit.SECONDS);
        mihomoInstalled = checkMihomo();
    }

    private boolean checkMihomo() {
        Process progress;
        try {
            progress = new ProcessBuilder("mihomo", "-v").start();
            if (progress.waitFor() == 0){
                mihomoPath = "mihomo";
                return true;
            }
        } catch (Exception ignored) {}
        try {
            progress = new ProcessBuilder("clash-meta", "-v").start();
            if (progress.waitFor() == 0){
                mihomoPath = "clash-meta";
                return true;
            }
        } catch (Exception ignored) {}
        var mihomoFile = new File("mihomo");
        if (mihomoFile.exists()) {
            if (!mihomoFile.canExecute() && !mihomoFile.setExecutable(true)) {
                log.error("set mihomo executable failed: path={}", mihomoPath);
                return false;
            }
            mihomoPath = mihomoFile.getAbsolutePath();
            return true;
        }
        log.warn("not found mihomo bin");
        return installMihomo();
    }

    private boolean checkGzip() {
        Process progress;
        try {
            progress = new ProcessBuilder("gzip", "--version").start();
            if (progress.waitFor() == 0){
                return true;
            }
        } catch (Exception ignored) {}
        return false;
    }

    boolean installMihomo() {
        log.info("install mihomo...");
        var httpClient = HttpClient.newHttpClient();
        if (!checkGzip()) {
            log.error("tar not installed. can not install mihomo.");
            return false;
        }
        try {
            log.info("get mihomo latest releases...");
            var request = HttpRequest.newBuilder().uri(URI.create("https://github.bit16.online/MetaCubeX/mihomo/releases/latest")).build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            Assert.isTrue(response.statusCode() == 200, "get mihomo releases failed. statusCode=" + response.statusCode());
            log.debug("mihomo releases content: {}", response.body());
            var releases = objectMapper.readValue(response.body(), GithubReleasesInfo.class);
            var downloadUrl = String.format("https://github.bit16.online/MetaCubeX/mihomo/releases/download/%s/mihomo-linux-amd64-%s.gz", releases.getTagName(), releases.getTagName());
            log.info("download mihomo package url={} ...", downloadUrl);
            request = HttpRequest.newBuilder()
                    .uri(URI.create(downloadUrl))
                    .build();
            var downloadResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (downloadResponse.statusCode() != 200){
                try(var body = downloadResponse.body()){
                    log.error("download releases failed. statusCode={}, body={}", downloadResponse.statusCode(), new String(body.readAllBytes()));
                }
                return false;
            }
            mihomoPath = System.getProperty("user.dir") + "/mihomo";
            var mihomoReleasesFile = mihomoPath + ".gz";
            try(var in = downloadResponse.body(); var out = new FileOutputStream(mihomoReleasesFile)) {
                in.transferTo(out);
            }
            Process tarProcess = new ProcessBuilder("gzip", "-d", mihomoReleasesFile).start();
            tarProcess.waitFor(5, TimeUnit.SECONDS);
            Assert.isTrue(tarProcess.exitValue() == 0, "unpack releases failed. statusCode=" + tarProcess.exitValue() + "\tmsg=" + new String(tarProcess.getErrorStream().readAllBytes()));
            var mohomoFile = new File(mihomoPath);
            Assert.isTrue(mohomoFile.setExecutable(true), "set mihomo executable failed.");
            log.info("install mihomo completed.");
            return true;
        } catch (IOException | InterruptedException e) {
            log.error("install mihomo failed.", e);
            return false;
        }
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

    private void mergeConfig(ClashSubscribeMergeConfig mergeConfig) {
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
        clashSubscribes.parallelStream()
                .filter(Objects::nonNull)
                .filter(s -> StringUtils.hasText(s.getUrl()))
                .peek(subscribe -> {
                    if (subscribe.getLatestUpdateTime() != null && subscribe.getLatestContent() != null && System.currentTimeMillis() - subscribe.getLatestUpdateTime().getTime() < 3600000L){
                        return;
                    }
                    try {
                        readSubscribe(subscribe);
                        subscribe.setStatus(ClashSubscribe.Status.NORMAL);
                        subscribe.setLatestUpdateTime(new Date());
                    } catch (Exception e) {
                        log.error("merge clash subscribe error. id={}", subscribe.getId(), e);
                        subscribe.setStatus(ClashSubscribe.Status.ABNORMAL);
                    }
                })
                .filter(subscribe -> subscribe.getLatestContent() != null && subscribe.getLatestContent().getProxies() != null && !subscribe.getLatestContent().getProxies().isEmpty())
                .map(subscribe->{
                    var proxies = subscribe.getLatestContent().getProxies();
                    if (!mihomoInstalled || mergeConfig.getLimitDelay() == null || mergeConfig.getLimitDelay() <= 0) {
                        return proxies;
                    }
                    var clashConfig = new ClashConfig();
                    clashConfig.setProxies(proxies);
                    var proxyGroup = new ProxyGroupConfig();
                    proxyGroup.setName("default");
                    proxyGroup.setType("select");
                    proxyGroup.setProxies(proxies.stream().map(ProxyConfig::getName).toList());
                    clashConfig.setProxyGroups(Collections.singletonList(proxyGroup));
                    Path mihomoWorkDir = null;
                    Process process = null;
                    try {
                        //获取一个随机端口
                        int port;
                        try(var serverSocket = new ServerSocket(0)){
                            port = serverSocket.getLocalPort();
                        }
                        mihomoWorkDir = Files.createTempDirectory("mihomo-test");
                        var configFile = mihomoWorkDir.resolve("config.yml");
                        Files.writeString(configFile, yaml.dump(clashConfig));
                        process = new ProcessBuilder(
                                mihomoPath,
                                "-f", configFile.toFile().getAbsolutePath(),
                                "-ext-ctl", "127.0.0.1:" + port,
                                "-d", mihomoWorkDir.toFile().getAbsolutePath()
                        )
                                .directory(mihomoWorkDir.toFile())
                                .start();
                        try {
                            process.waitFor(1, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            log.error("merge clash subscribe error.", e);
                            return proxies;
                        }
                        if (!process.isAlive()){
                            log.error("check proxy delay mihomo process error: exitCode={}\terror={}", process.exitValue(), new String(process.getErrorStream().readAllBytes()));
                            return proxies;
                        }
                        var httpClient = HttpClient.newHttpClient();
                        var request = HttpRequest.newBuilder()
                                .uri(URI.create("http://127.0.0.1:" + port + "/group/default/delay?url=https://www.gstatic.com/generate_204&timeout=5000"))
                                .build();
                        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
                        var delayMap = objectMapper.readValue(response.body(), new TypeReference<Map<String, Integer>>() {});
                        subscribe.setLatestDelay(delayMap);
                        process.destroy();
                        return proxies.stream().filter(p->{
                            var delay = delayMap.get(p.getName());
                            return delay != null && delay < mergeConfig.getLimitDelay();
                        }).toList();
                    } catch (Exception e) {
                        log.error("check proxy delay failed.", e);
                        return proxies;
                    }finally {
                        if (process != null && process.isAlive()) {
                            process.destroy();
                        }
                        if (mihomoWorkDir != null){
                            try(var tempFiles = Files.walk(mihomoWorkDir)) {
                                tempFiles.sorted(Comparator.reverseOrder()).forEach(path -> {
                                    try {
                                        Files.delete(path);
                                    } catch (IOException e) {
                                        log.error("delete temp config file failed.", e);
                                    }
                                });
                            } catch (IOException e) {
                                log.error("delete temp config file failed.", e);
                            }
                        }
                        clashSubscribeMapper.updateById(subscribe);
                    }
                })
                .forEach(allProxies::addAll);
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
        if (filtered.isEmpty()) {
            throw new RuntimeException("merge clash subscribe error: no match proxies");
        }
        // 构造新的ClashConfig
        ClashConfig override = mergeConfig.getOverrideConfig() != null && !mergeConfig.getOverrideConfig().isEmpty()
                ? yaml.load(new ByteArrayInputStream(mergeConfig.getOverrideConfig().getBytes()))
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
        String newYaml = yaml.dump(override);
        mergeConfig.setLatestMergeContent(newYaml);
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

    private void readSubscribe(ClashSubscribe subscribe) throws IOException, InterruptedException {
        subscribe.setLatestContent(null);
        var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        var request = HttpRequest.newBuilder()
                .uri(URI.create(subscribe.getUrl()))
                .header("User-Agent", "Clash-Verge: v2.2.3")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
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
        if (content == null) return;
        var strContent = new String(content, StandardCharsets.UTF_8);
        if (strContent.matches("^([a-zA-Z0-9/=]{4})+$")){
            var proxies = parseBase64Content(subscribe, strContent);
            var tempConfig = new ClashConfig();
            tempConfig.setProxies(proxies);
            subscribe.setLatestContent(tempConfig);
            return;
        }
        ClashConfig config;
        try {
            config = yaml.load(new ByteArrayInputStream(content));
        }catch (Exception e){
            log.error("read subscribe error: content={}", new String(content), e);
            return;
        }
        var proxies = config.getProxies();
        if (proxies == null || proxies.isEmpty()) return;
        if (subscribe.getSkipProxies() != null && subscribe.getSkipProxies() < proxies.size()) {
            proxies = proxies.subList(subscribe.getSkipProxies(), proxies.size());
            config.setProxies(proxies);
        }
        subscribe.setLatestContent(config);
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
        var result = clashSubscribeMergeConfigMapper.selectPage(page, new QueryWrapper<ClashSubscribeMergeConfig>()
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
                                    .reduce(new ArrayList<>(), (a, b)->{
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
    public ClashYamlContentVo getContent(String token) {
        var target = clashSubscribeMergeConfigMapper.selectOne(new QueryWrapper<ClashSubscribeMergeConfig>().eq("token", token));
        if (target == null) return null;
        var vo = new ClashYamlContentVo();
        vo.setContent(target.getLatestMergeContent());
        var subscribes = getClashSubscribes(target);
        vo.setDownloadTraffic(subscribes.stream().mapToLong(ClashSubscribe::getDownloadTraffic).sum());
        vo.setUploadTraffic(subscribes.stream().mapToLong(ClashSubscribe::getUploadTraffic).sum());
        vo.setTotalTraffic(subscribes.stream().mapToLong(ClashSubscribe::getTotalTraffic).sum());
        vo.setExpire(subscribes.stream().mapToLong(s->s.getExpiredTime().getTime()).max().orElse(-1));
        return vo;
    }

    @Override
    public ClashSubscribeMergeConfig add(ClashSubscribeMergeConfigDto dto) {
        ClashSubscribeMergeConfig entity = new ClashSubscribeMergeConfig();
        BeanUtils.copyProperties(dto, entity);
        // token 需要生成，简单用UUID，实际可自定义
        entity.setToken(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        clashSubscribeMergeConfigMapper.insert(entity);
        return entity;
    }

    @Override
    public ClashSubscribeMergeConfig update(ClashSubscribeMergeConfigDto dto) {
        Assert.notNull(dto, "参数不能为空");
        Assert.notNull(dto.getName(), "名称不能为空");
        ClashSubscribeMergeConfig entity = clashSubscribeMergeConfigMapper.selectOne(new QueryWrapper<ClashSubscribeMergeConfig>().eq("name", dto.getName()));
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
        if ("all".equalsIgnoreCase(target.getSubscribeIds())){
            vo.setSubscribeNames("全部订阅");
        }else{
            List<ClashSubscribe> subscribes = getClashSubscribes(target);
            if (subscribes.size() > 3) {
                vo.setSubscribeNames(subscribes.subList(0, 3).stream().map(ClashSubscribe::getName).collect(Collectors.joining("、")) + "等" + subscribes.size() + "个订阅");
            }else{
                vo.setSubscribeNames(subscribes.stream().map(ClashSubscribe::getName).collect(Collectors.joining("、")));
            }
        }
        return vo;
    }

    private List<ClashSubscribe> getClashSubscribes(ClashSubscribeMergeConfig target) {
        List<ClashSubscribe> subscribes;
        if ("all".equalsIgnoreCase(target.getSubscribeIds())) {
            subscribes = clashSubscribeMapper.selectList(Wrappers.emptyWrapper());
        }else{
            subscribes = clashSubscribeMapper.selectByIds(
                    Stream.of(target.getSubscribeIds().split(",")).map(Long::valueOf).toList()
            );
        }
        return subscribes;
    }

    @Override
    public String getTokenById(Long id) {
        var target = clashSubscribeMergeConfigMapper.selectById(id);
        if (target == null) return null;
        return target.getToken();
    }
}