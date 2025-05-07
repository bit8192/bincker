package cn.bincker.modules.clash.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DnsConfig {
    @JsonProperty("cache-algorithm")
    private String cacheAlgorithm;
    private Boolean enable;
    @JsonProperty("prefer-h3")
    private Boolean preferH3;
    private String listen;
    private Boolean ipv6;
    @JsonProperty("ipv6-timeout")
    private Integer ipv6Timeout;
    @JsonProperty("default-nameserver")
    private List<String> defaultNameserver;
    @JsonProperty("enhanced-mode")
    private String enhancedMode;
    @JsonProperty("fake-ip-range")
    private String fakeIpRange;
    @JsonProperty("fake-ip-filter")
    private List<String> fakeIpFilter;
    @JsonProperty("fake-ip-filter-mode")
    private String fakeIpFilterMode;
    @JsonProperty("use-hosts")
    private Boolean useHosts;
    @JsonProperty("respect-rules")
    private Boolean respectRules;
    private List<String> nameserver;
    private List<String> fallback;
    @JsonProperty("proxy-server-nameserver")
    private List<String> proxyServerNameserver;
    @JsonProperty("direct-nameserver")
    private List<String> directNameserver;
    @JsonProperty("direct-nameserver-follow-policy")
    private Boolean directNameserverFollowPolicy;
    @JsonProperty("fallback-filter")
    private FallbackFilterConfig fallbackFilter;
    @JsonProperty("nameserver-policy")
    private Map<String, Object> nameserverPolicy;
}

@Data
class FallbackFilterConfig {
    private Boolean geoip;
    @JsonProperty("geoip-code")
    private String geoipCode;
    private List<String> geosite;
    private List<String> ipcidr;
    private List<String> domain;
} 