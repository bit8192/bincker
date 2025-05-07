package cn.bincker.modules.clash.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class SnifferConfig {
    private Boolean enable;
    @JsonProperty("force-dns-mapping")
    private Boolean forceDnsMapping;
    @JsonProperty("parse-pure-ip")
    private Boolean parsePureIp;
    @JsonProperty("override-destination")
    private Boolean overrideDestination;
    private Map<String, SniffConfig> sniff;
    @JsonProperty("force-domain")
    private List<String> forceDomain;
    @JsonProperty("skip-src-address")
    private List<String> skipSrcAddress;
    @JsonProperty("skip-dst-address")
    private List<String> skipDstAddress;
    @JsonProperty("skip-domain")
    private List<String> skipDomain;
    private List<String> sniffing;
    @JsonProperty("port-whitelist")
    private List<String> portWhitelist;
}

@Data
class SniffConfig {
    private List<String> ports;
    @JsonProperty("override-destination")
    private Boolean overrideDestination;
} 