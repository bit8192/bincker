package cn.bincker.modules.clash.entity.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ListenerConfig {
    private String name;
    private String type;
    private String port;
    private String listen;
    private String rule;
    private String proxy;
    private Boolean udp;
    @JsonDeserialize(using = UsersConfigDeserialize.class)
    private List<UserConfig> users;
    private String certificate;
    @JsonProperty("private-key")
    private String privateKey;
    private List<String> network;
    private String target;
    @JsonProperty("ws-opts")
    private Map<String, Object> wsOpts;
    @JsonProperty("grpc-service-name")
    private String grpcServiceName;
    @JsonProperty("reality-config")
    private RealityConfig realityConfig;
    private String token;
    @JsonProperty("congestion-controller")
    private String congestionController;
    @JsonProperty("max-idle-time")
    private Integer maxIdleTime;
    @JsonProperty("authentication-timeout")
    private Integer authenticationTimeout;
    private List<String> alpn;
    @JsonProperty("max-udp-relay-packet-size")
    private Integer maxUdpRelayPacketSize;
    @JsonProperty("fast-open")
    private Boolean fastOpen;
    private String stack;
    @JsonProperty("dns-hijack")
    private List<String> dnsHijack;
    @JsonProperty("auto-detect-interface")
    private Boolean autoDetectInterface;
    @JsonProperty("auto-route")
    private Boolean autoRoute;
    private Integer mtu;
    @JsonProperty("inet4-address")
    private List<String> inet4Address;
    @JsonProperty("inet6-address")
    private List<String> inet6Address;
    @JsonProperty("strict-route")
    private Boolean strictRoute;
    @JsonProperty("inet4-route-address")
    private List<String> inet4RouteAddress;
    @JsonProperty("inet6-route-address")
    private List<String> inet6RouteAddress;
    @JsonProperty("endpoint-independent-nat")
    private Boolean endpointIndependentNat;
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
    private String password;
    private String cipher;

    public static class UsersConfigDeserialize extends JsonDeserializer<List<UserConfig>> {
        @Override
        public List<UserConfig> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            List<UserConfig> users = new ArrayList<>();

            if (node.isArray()) {
                for (JsonNode user : node) {
                    UserConfig userConfig = new UserConfig();
                    if (node.has("username")) userConfig.setUsername(user.get("username").asText());
                    if (node.has("password")) userConfig.setUsername(user.get("password").asText());
                    if (node.has("uuid")) userConfig.setUsername(user.get("uuid").asText());
                    if (node.has("alterId")) userConfig.setUsername(user.get("alterId").asText());
                    if (node.has("flow")) userConfig.setUsername(user.get("flow").asText());
                    users.add(userConfig);
                }
            }
            // 如果是对象，说明是完整YAML格式
            else if (node.isObject()) {
                node.fieldNames().forEachRemaining(name -> {
                    UserConfig userConfig = new UserConfig();
                    userConfig.setUsername(name);
                    userConfig.setUsername(node.get(name).asText());
                    users.add(userConfig);
                });
            }

            return users;
        }
    }
}

@Data
class UserConfig {
    private String username;
    private String password;
    private String uuid;
    private Integer alterId;
    private String flow;
}

@Data
class RealityConfig {
    private String dest;
    @JsonProperty("private-key")
    private String privateKey;
    @JsonProperty("short-id")
    private List<String> shortId;
    @JsonProperty("server-names")
    private List<String> serverNames;
} 