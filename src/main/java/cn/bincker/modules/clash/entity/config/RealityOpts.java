package cn.bincker.modules.clash.entity.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RealityOpts {
    @JsonProperty("public-key")
    private String publicKey;
    @JsonProperty("short-id")
    private String shortId;
}
