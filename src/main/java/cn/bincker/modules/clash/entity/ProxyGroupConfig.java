package cn.bincker.modules.clash.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProxyGroupConfig {
    private String name;
    private String type;
    private List<String> proxies;
    private Integer tolerance;
    private Boolean lazy;
    @JsonProperty("expected-status")
    private Integer expectedStatus;
    private String url;
    private Integer interval;
    private String strategy;
    @JsonProperty("disable-udp")
    private Boolean disableUdp;
    @JsonProperty("interface-name")
    private String interfaceName;
    @JsonProperty("routing-mark")
    private Integer routingMark;
    private String filter;
    private List<String> use;
    @JsonProperty("health-check")
    private HealthCheckConfig healthCheck;
    private Map<String, Object> override;
}

@Data
class HealthCheckConfig {
    private Boolean enable;
    private Integer interval;
    private Boolean lazy;
    private String url;
    @JsonProperty("expected-status")
    private Integer expectedStatus;
} 