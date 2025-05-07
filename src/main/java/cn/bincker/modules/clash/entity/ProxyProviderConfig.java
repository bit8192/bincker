package cn.bincker.modules.clash.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProxyProviderConfig {
    private String type;
    private String url;
    private Integer interval;
    private String path;
    private String proxy;
    @JsonProperty("size-limit")
    private Integer sizeLimit;
    private Map<String, List<String>> header;
    @JsonProperty("health-check")
    private HealthCheckConfig healthCheck;
    private Map<String, Object> override;
    @JsonProperty("dialer-proxy")
    private String dialerProxy;
    private List<ProxyConfig> payload;
} 