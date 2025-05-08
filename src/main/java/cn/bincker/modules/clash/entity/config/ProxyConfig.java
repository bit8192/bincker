package cn.bincker.modules.clash.entity.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ProxyConfig {
    private String name;
    private String servername;
    private String type;
    private String server;
    private Integer port;
    private String ports;
    private String username;
    private String password;
    private Boolean tls;
    private String fingerprint;
    @JsonProperty("skip-cert-verify")
    private Boolean skipCertVerify;
    private Boolean udp;
    @JsonProperty("ip-version")
    private String ipVersion;
    private String sni;
    private List<String> alpn;
    private String uuid;
    private Integer alterId;
    private String cipher;
    private String psk;
    private Integer version;
    @JsonProperty("obfs-opts")
    private Map<String, Object> obfsOpts;
    private String plugin;
    @JsonProperty("plugin-opts")
    private Map<String, Object> pluginOpts;
    private String network;
    @JsonProperty("ws-opts")
    private Map<String, Object> wsOpts;
    @JsonProperty("h2-opts")
    private Map<String, Object> h2Opts;
    @JsonProperty("grpc-opts")
    private Map<String, Object> grpcOpts;
    @JsonProperty("http-opts")
    private Map<String, Object> httpOpts;
    private String flow;
    @JsonProperty("flow-show")
    private Boolean flowShow;
    private String protocol;
    private String up;
    private String down;
    @JsonProperty("auth-str")
    private String authStr;
    private String obfs;
    @JsonProperty("obfs-password")
    private String obfsPassword;
    @JsonProperty("hop-interval")
    private String hopInterval;
    @JsonProperty("congestion-controller")
    private String congestionController;
    @JsonProperty("max-idle-time")
    private Integer maxIdleTime;
    @JsonProperty("authentication-timeout")
    private Integer authenticationTimeout;
    @JsonProperty("max-udp-relay-packet-size")
    private Integer maxUdpRelayPacketSize;
    @JsonProperty("fast-open")
    private Boolean fastOpen;
    private String ip;
    private String ipv6;
    @JsonProperty("public-key")
    private String publicKey;
    @JsonProperty("private-key")
    private String privateKey;
    @JsonProperty("pre-shared-key")
    private String preSharedKey;
    private String reserved;
    @JsonProperty("dialer-proxy")
    private String dialerProxy;
    @JsonProperty("remote-dns-resolve")
    private Boolean remoteDnsResolve;
    private List<String> dns;
    @JsonProperty("refresh-server-ip-interval")
    private Integer refreshServerIpInterval;
    private List<PeerConfig> peers;
    @JsonProperty("amnezia-wg-option")
    private AmneziaWgOptionConfig amneziaWgOption;
    private SmuxConfig smux;
    @JsonProperty("client-fingerprint")
    private String clientFingerprint;
    @JsonProperty("reality-opts")
    private RealityOpts realityOpts;
    private String token;
    @JsonProperty("disable-sni")
    private Boolean disableSni;
    @JsonProperty("reduce-rtt")
    private Boolean reduceRtt;
    @JsonProperty("request-timeout")
    private Integer requestTimeout;
    @JsonProperty("udp-relay-mode")
    private String udpRelayMode;
    private String transport;
    @JsonProperty("heartbeat-interval")
    private Integer heartbeatInterval;
    @JsonProperty("max-open-streams")
    private Integer maxOpenStreams;
    private Integer mtu;
}

@Data
class PeerConfig {
    private String server;
    private Integer port;
    @JsonProperty("public-key")
    private String publicKey;
    @JsonProperty("pre-shared-key")
    private String preSharedKey;
    @JsonProperty("allowed-ips")
    private List<String> allowedIps;
    private String reserved;
}

@Data
class AmneziaWgOptionConfig {
    private Integer jc;
    private Integer jmin;
    private Integer jmax;
    private Integer s1;
    private Integer s2;
    private Integer h1;
    private Integer h2;
    private Integer h3;
    private Integer h4;
}

@Data
class SmuxConfig {
    private Boolean enabled = false;
    private String protocol = "smux";
    @JsonProperty("max-connections")
    private Integer maxConnections = 4;
    @JsonProperty("min-streams")
    private Integer minStreams = 4;
    @JsonProperty("max-streams")
    private Integer maxStreams = 0;
    private Boolean padding = false;
    private Boolean statistic = false;
    @JsonProperty("only-tcp")
    private Boolean onlyTcp = false;
}

