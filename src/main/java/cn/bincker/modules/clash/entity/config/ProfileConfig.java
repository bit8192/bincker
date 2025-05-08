package cn.bincker.modules.clash.entity.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProfileConfig {
    @JsonProperty("store-selected")
    private Boolean storeSelected;
    @JsonProperty("store-fake-ip")
    private Boolean storeFakeIp;
} 