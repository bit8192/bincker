package cn.bincker.modules.clash.entity.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FallbackFilterConfig {
    private Boolean geoip;
    @JsonProperty("geoip-code")
    private String geoipCode;
    private List<String> geosite;
    private List<String> ipcidr;
    private List<String> domain;
}
