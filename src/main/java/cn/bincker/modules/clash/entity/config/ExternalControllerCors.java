package cn.bincker.modules.clash.entity.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ExternalControllerCors {
    @JsonProperty("allow-origins")
    private List<String> allowOrigins;
    @JsonProperty("allow-private-network")
    private Boolean allowPrivateNetwork;
} 