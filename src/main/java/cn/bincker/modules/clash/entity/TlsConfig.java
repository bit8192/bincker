package cn.bincker.modules.clash.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TlsConfig {
    private String certificate;
    @JsonProperty("private-key")
    private String privateKey;
    @JsonProperty("custom-certificates")
    private List<String> customCertificates;
} 