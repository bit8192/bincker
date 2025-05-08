package cn.bincker.modules.clash.entity.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ClashConfig {
    @JsonProperty("mixed-port")
    private Integer mixedPort;
    
    @JsonProperty("allow-lan")
    private Boolean allowLan;
    
    @JsonProperty("bind-address")
    private String bindAddress;
    
    private List<String> authentication;
    
    @JsonProperty("skip-auth-prefixes")
    private List<String> skipAuthPrefixes;
    
    @JsonProperty("lan-allowed-ips")
    private List<String> lanAllowedIps;
    
    @JsonProperty("lan-disallowed-ips")
    private List<String> lanDisallowedIps;
    
    @JsonProperty("find-process-mode")
    private String findProcessMode;
    
    private String mode;
    
    @JsonProperty("geox-url")
    private Map<String, String> geoxUrl;
    
    @JsonProperty("geo-auto-update")
    private Boolean geoAutoUpdate;
    
    @JsonProperty("geo-update-interval")
    private Integer geoUpdateInterval;
    
    @JsonProperty("log-level")
    private String logLevel;
    
    private Boolean ipv6;
    
    private TlsConfig tls;
    
    @JsonProperty("external-controller")
    private String externalController;
    
    @JsonProperty("external-controller-tls")
    private String externalControllerTls;
    
    private String secret;
    
    @JsonProperty("external-controller-cors")
    private ExternalControllerCors externalControllerCors;
    
    @JsonProperty("external-controller-unix")
    private String externalControllerUnix;
    
    @JsonProperty("external-controller-pipe")
    private String externalControllerPipe;
    
    @JsonProperty("tcp-concurrent")
    private Boolean tcpConcurrent;
    
    @JsonProperty("external-ui")
    private String externalUi;
    
    @JsonProperty("external-ui-name")
    private String externalUiName;
    
    @JsonProperty("external-ui-url")
    private String externalUiUrl;
    
    @JsonProperty("external-doh-server")
    private String externalDohServer;
    
    @JsonProperty("interface-name")
    private String interfaceName;
    
    @JsonProperty("global-client-fingerprint")
    private String globalClientFingerprint;
    
    @JsonProperty("disable-keep-alive")
    private Boolean disableKeepAlive;
    
    @JsonProperty("keep-alive-idle")
    private Integer keepAliveIdle;
    
    @JsonProperty("keep-alive-interval")
    private Integer keepAliveInterval;
    
    @JsonProperty("routing-mark")
    private Integer routingMark;
    
    private ExperimentalConfig experimental;
    
    private Map<String, Object> hosts;
    
    private ProfileConfig profile;
    
    private TunConfig tun;
    
    private SnifferConfig sniffer;
    
    private List<TunnelConfig> tunnels;
    
    private DnsConfig dns;
    
    private List<ProxyConfig> proxies;
    
    @JsonProperty("proxy-groups")
    private List<ProxyGroupConfig> proxyGroups;
    
    @JsonProperty("proxy-providers")
    private Map<String, ProxyProviderConfig> proxyProviders;
    
    @JsonProperty("rule-providers")
    private Map<String, RuleProviderConfig> ruleProviders;
    
    private List<Object> rules;
    
    @JsonProperty("sub-rules")
    private Map<String, List<Object>> subRules;
    
    private List<ListenerConfig> listeners;
} 