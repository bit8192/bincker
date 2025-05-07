package cn.bincker.modules.clash.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TunConfig {
    private Boolean enable;
    private String stack;
    @JsonProperty("dns-hijack")
    private List<String> dnsHijack;
    @JsonProperty("auto-detect-interface")
    private Boolean autoDetectInterface;
    @JsonProperty("auto-route")
    private Boolean autoRoute;
    private Integer mtu;
    private Boolean gso;
    @JsonProperty("gso-max-size")
    private Integer gsoMaxSize;
    @JsonProperty("auto-redirect")
    private Boolean autoRedirect;
    @JsonProperty("strict-route")
    private Boolean strictRoute;
    @JsonProperty("route-address-set")
    private List<String> routeAddressSet;
    @JsonProperty("route-exclude-address-set")
    private List<String> routeExcludeAddressSet;
    @JsonProperty("route-address")
    private List<String> routeAddress;
    @JsonProperty("endpoint-independent-nat")
    private Boolean endpointIndependentNat;
    @JsonProperty("include-interface")
    private List<String> includeInterface;
    @JsonProperty("exclude-interface")
    private List<String> excludeInterface;
    @JsonProperty("include-uid")
    private List<Integer> includeUid;
    @JsonProperty("include-uid-range")
    private List<String> includeUidRange;
    @JsonProperty("exclude-uid")
    private List<Integer> excludeUid;
    @JsonProperty("exclude-uid-range")
    private List<String> excludeUidRange;
    @JsonProperty("include-android-user")
    private List<Integer> includeAndroidUser;
    @JsonProperty("include-package")
    private List<String> includePackage;
    @JsonProperty("exclude-package")
    private List<String> excludePackage;
} 