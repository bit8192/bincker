package cn.bincker.modules.clash.utils;

import cn.bincker.modules.clash.entity.config.ProxyConfig;
import cn.bincker.modules.clash.entity.config.RealityOpts;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ProxyUrlParser {

    public static ProxyConfig parseUri(String uri) {
        String head = uri.split("://")[0];
        return switch (head) {
            case "ss" -> uriSS(uri);
            case "ssr" -> uriSSR(uri);
            case "vmess" -> uriVMess(uri);
            case "vless" -> uriVLess(uri);
            case "trojan" -> uriTrojan(uri);
            case "hysteria2", "hy2" -> uriHysteria2(uri);
            case "hysteria", "hy" -> uriHysteria(uri);
            case "tuic" -> uriTUIC(uri);
            case "wireguard", "wg" -> uriWireguard(uri);
            case "http" -> uriHTTP(uri);
            case "socks5" -> uriSOCKS(uri);
            default -> throw new IllegalArgumentException("Unknown uri type: " + head);
        };
    }

    private static String getIfNotBlank(String ...values) {
        for (String value : values) {
            if (StringUtils.hasText(value)){
                return value;
            }
        }
        if (values.length > 0) {
            return values[values.length-1];
        }
        return null;
    }

    private static <T> T getIfPresent(T value, T dft) {
        return value != null ? value : dft;
    }

    private static boolean isPresent(Object value) {
        return value != null;
    }

    private static boolean isIPv4(String address) {
        Pattern ipv4Regex = Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$");
        return ipv4Regex.matcher(address).matches();
    }

    private static boolean isIPv6(String address) {
        Pattern ipv6Regex = Pattern.compile(
                "^(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +                 // 标准格式
                        "^((?:[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4})*)?)::((?:[0-9a-fA-F]{1,4}(?::[0-9a-fA-F]{1,4})*)?)$|" +  // 双冒号缩写
                        "^(?:[0-9a-fA-F]{1,4}:){6}(?:[0-9]+\\.){3}[0-9]+$|" +          // IPv4映射的IPv6地址
                        "^::(?:ffff:(?:[0-9]+\\.){3}[0-9]+)$"                        // 另一种IPv4映射格式
        );
        return ipv6Regex.matcher(address).matches();
    }

    private static String decodeBase64OrOriginal(String str) {
        try {
            return new String(Base64.getDecoder().decode(str));
        } catch (Exception e) {
            return str;
        }
    }

    private static String getCipher(String str) {
        if (str == null) return "none";

        return switch (str) {
            case "none", "auto", "dummy", "aes-128-gcm", "aes-192-gcm", "aes-256-gcm", "lea-128-gcm", "lea-192-gcm",
                 "lea-256-gcm", "aes-128-gcm-siv", "aes-256-gcm-siv", "2022-blake3-aes-128-gcm",
                 "2022-blake3-aes-256-gcm", "aes-128-cfb", "aes-192-cfb", "aes-256-cfb", "aes-128-ctr", "aes-192-ctr",
                 "aes-256-ctr", "chacha20", "chacha20-poly1305", "chacha20-ietf", "chacha20-ietf-poly1305",
                 "2022-blake3-chacha20-poly1305", "rabbit128-poly1305", "xchacha20-ietf-poly1305", "xchacha20",
                 "aegis-128l", "aegis-256", "aez-384", "deoxys-ii-256-128", "rc4-md5" -> str;
            default -> "auto";
        };
    }

    private static ProxyConfig uriSS(String line) {
        line = line.split("ss://")[1];
        String content = line.split("#")[0];
        String name = decodeURIComponent(line.split("#")[1]);

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("ss");
        proxy.setName(name);

        String serverAndPort = content;
        if (content.contains("@")) {
            serverAndPort = content.split("@")[1];
            content = content.split("@")[0];
        }

        String[] serverPortArray = serverAndPort.split(":");
        proxy.setServer(serverPortArray[0]);
        proxy.setPort(Integer.parseInt(serverPortArray[1]));

        String userInfoStr = decodeBase64OrOriginal(content);
        String[] userInfo = userInfoStr.split(":");
        proxy.setCipher(getCipher(userInfo[0]));
        proxy.setPassword(userInfo[1]);

        return proxy;
    }

    private static ProxyConfig uriSSR(String line) {
        line = decodeBase64OrOriginal(line.split("ssr://")[1]);

        int splitIdx = line.indexOf(":origin");
        if (splitIdx == -1) {
            splitIdx = line.indexOf(":auth_");
        }

        String serverAndPort = line.substring(0, splitIdx);
        String server = serverAndPort.substring(0, serverAndPort.lastIndexOf(":"));
        int port = Integer.parseInt(serverAndPort.substring(serverAndPort.lastIndexOf(":") + 1));

        String[] params = line.substring(splitIdx + 1).split("/?")[0].split(":");

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("ssr");
        proxy.setServer(server);
        proxy.setPort(port);
        proxy.setProtocol(params[0]);
        proxy.setCipher(getCipher(params[1]));
        proxy.setObfs(params[2]);
        proxy.setPassword(decodeBase64OrOriginal(params[3]));

        return proxy;
    }

    private static ProxyConfig uriVMess(String line) {
        line = line.split("vmess://")[1];
        String content = decodeBase64OrOriginal(line);

        if (content.contains("vmess")) {
            // Quantumult VMess URI format
            String[] partitions = content.split(",");
            Map<String, String> params = new HashMap<>();
            for (String part : partitions) {
                if (part.contains("=")) {
                    String[] keyValue = part.split("=");
                    params.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }

            ProxyConfig proxy = new ProxyConfig();
            proxy.setType("vmess");
            proxy.setName(partitions[0].split("=")[0].trim());
            proxy.setServer(partitions[1]);
            proxy.setPort(Integer.parseInt(partitions[2]));
            proxy.setCipher(getCipher(getIfNotBlank(partitions[3], "auto")));
            proxy.setUuid(partitions[4].replaceAll("^\"|\"$", ""));

            proxy.setTls("wss".equals(params.get("obfs")));
            proxy.setUdp(params.containsKey("udp-relay"));
            proxy.setFastOpen(params.containsKey("fast-open"));
            proxy.setSkipCertVerify(isPresent(params.get("tls-verification")) ? !Boolean.parseBoolean(params.get("tls-verification")) : null);

            if ("ws".equals(params.get("obfs")) || "wss".equals(params.get("obfs"))) {
                proxy.setNetwork("ws");
                Map<String, Object> wsOpts = new HashMap<>();
                wsOpts.put("path", getIfNotBlank(params.get("obfs-path"), "/").replaceAll("^\"|\"$", "/"));
                wsOpts.put("headers", Map.of("Host", params.get("obfs-header") != null ? params.get("obfs-header").replaceAll("Host:\\s*", "") : ""));
                proxy.setWsOpts(wsOpts);
            } else {
                throw new IllegalArgumentException("Unsupported obfs: " + params.get("obfs"));
            }

            return proxy;
        } else {
            // V2rayN URI format
            try {
                Map<String, Object> params = new ObjectMapper().readValue(content, Map.class);

                ProxyConfig proxy = new ProxyConfig();
                proxy.setType("vmess");
                proxy.setName(getIfNotBlank((String) params.get("ps"), getIfNotBlank((String) params.get("remarks"), getIfNotBlank((String) params.get("remark"), "VMess " + params.get("add") + ":" + params.get("port")))));
                proxy.setServer((String) params.get("add"));
                proxy.setPort(Integer.parseInt((String) params.get("port")));
                proxy.setCipher(getCipher(getIfPresent((String) params.get("scy"), "auto")));
                proxy.setUuid((String) params.get("id"));
                proxy.setTls("tls".equals(params.get("tls")) || Boolean.parseBoolean((String) params.get("tls")));
                proxy.setSkipCertVerify(isPresent(params.get("verify_cert")) ? !Boolean.parseBoolean((String) params.get("verify_cert")) : null);

                proxy.setAlterId(Integer.parseInt(getIfPresent((String) params.get("aid"), getIfPresent((String) params.get("alterId"), "0"))));

                if (proxy.getTls() != null && proxy.getTls() && params.get("sni") != null) {
                    proxy.setServername((String) params.get("sni"));
                }

                if ("ws".equals(params.get("net")) || "websocket".equals(params.get("obfs"))) {
                    proxy.setNetwork("ws");
                } else if ("http".equals(params.get("net")) || "http".equals(params.get("obfs")) || "http".equals(params.get("type"))) {
                    proxy.setNetwork("http");
                } else if ("grpc".equals(params.get("net"))) {
                    proxy.setNetwork("grpc");
                } else if ("httpupgrade".equals(params.get("net"))) {
                    proxy.setNetwork("ws");
                } else if ("h2".equals(params.get("net")) || "h2".equals(proxy.getNetwork())) {
                    proxy.setNetwork("h2");
                }

                if (proxy.getNetwork() != null) {
                    String transportHost = (String) params.get("host");
                    String transportPath = (String) params.get("path");

                    if (proxy.getNetwork().equals("grpc")) {
                        Map<String, Object> grpcOpts = new HashMap<>();
                        grpcOpts.put("grpc-service-name", getIfNotBlank(transportPath));
                        proxy.setGrpcOpts(grpcOpts);
                    } else {
                        Map<String, Object> opts = new HashMap<>();
                        opts.put("path", getIfNotBlank(transportPath));
                        opts.put("headers", Map.of("Host", Objects.requireNonNull(getIfNotBlank(transportHost, ""))));
                        switch (proxy.getNetwork()) {
                            case "ws":
                                proxy.setWsOpts(opts);
                                break;
                            case "http":
                                proxy.setHttpOpts(opts);
                                break;
                            case "h2":
                                proxy.setH2Opts(opts);
                                break;
                        }
                    }
                }

                return proxy;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid VMess URI format", e);
            }
        }
    }

    private static ProxyConfig uriVLess(String line) {
        line = line.split("vless://")[1];
        Matcher matcher = Pattern.compile("^(.*?)@(.*?):(\\d+)/?(\\?(.*?))?(?:#(.*?))?$").matcher(line);
        if (!matcher.find()) {
            String[] parts = line.split("\\?", 2);
            line = atob(parts[0]) + (parts.length > 1 ? "?" + parts[1] : "");
            matcher = Pattern.compile("^(.*?)@(.*?):(\\d+)/?(\\?(.*?))?(?:#(.*?))?$").matcher(line);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Invalid VLess URI format");
            }
        }

        String uuid = matcher.group(1);
        String server = matcher.group(2);
        int port = Integer.parseInt(matcher.group(3));
        String addons = matcher.group(5);
        String name = matcher.group(6);
        if (addons != null) addons = "";

        Map<String, String> params = new HashMap<>();
        if (addons != null) {
            for (String addon : addons.split("&")) {
                String[] keyValue = addon.split("=");
                params.put(keyValue[0], decodeURIComponent(keyValue[1]));
            }
        }

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("vless");
        proxy.setName(getIfNotBlank(decodeURIComponent(name), params.get("remarks"), params.get("remark"), "VLESS " + server + ":" + port));
        proxy.setServer(server);
        proxy.setPort(port);
        proxy.setUuid(uuid);

        proxy.setTls(!"none".equals(params.get("security")));
        proxy.setServername(params.get("sni"));
        proxy.setFlow(params.get("flow") != null ? "xtls-rprx-vision" : null);

        proxy.setClientFingerprint(params.get("fp"));
        proxy.setAlpn(params.get("alpn") != null ? Arrays.asList(params.get("alpn").split(",")) : null);
        proxy.setSkipCertVerify("1".equals(params.get("allowInsecure")));

        if ("reality".equals(params.get("security"))) {
            RealityOpts realityOpts = new RealityOpts();
            realityOpts.setPublicKey(params.get("pbk"));
            realityOpts.setShortId(params.get("sid"));
            proxy.setRealityOpts(realityOpts);
        }

        return proxy;
    }

    private static ProxyConfig uriTrojan(String line) {
        line = line.split("trojan://")[1];
        Matcher matcher = Pattern.compile("^(.*?)@(.*?)(:(\\d+))?/?(\\?(.*?))?(?:#(.*?))?$").matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid Trojan URI format");
        }

        String password = matcher.group(1);
        String server = matcher.group(2);
        int port = Integer.parseInt(matcher.group(4));
        String addons = matcher.group(6);
        String name = matcher.group(7);

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("trojan");
        proxy.setName(getIfNotBlank(decodeURIComponent(name), "Trojan " + server + ":" + port));
        proxy.setServer(server);
        proxy.setPort(port);
        proxy.setPassword(password);

        Map<String, String> params = new HashMap<>();
        if (addons != null) {
            for (String addon : addons.split("&")) {
                String[] keyValue = addon.split("=");
                params.put(keyValue[0], decodeURIComponent(keyValue[1]));
            }
        }

        proxy.setNetwork("tcp");
        if ("ws".equals(params.get("type")) || "h2".equals(params.get("type"))) {
            proxy.setNetwork(params.get("type"));
        }

        if ("ws".equals(proxy.getNetwork())) {
            Map<String, Object> wsOpts = new HashMap<>();
            wsOpts.put("headers", Map.of("Host", params.get("host")));
            wsOpts.put("path", params.get("path"));
            proxy.setWsOpts(wsOpts);
        }

        proxy.setAlpn(params.get("alpn") != null ? Arrays.asList(params.get("alpn").split(",")) : null);
        proxy.setSni(params.get("sni"));
        proxy.setSkipCertVerify("1".equals(params.get("skip-cert-verify")));
        proxy.setFingerprint(params.get("fingerprint"));

        return proxy;
    }

    private static ProxyConfig uriHysteria2(String line) {
        line = line.split("(hysteria2|hy2)://")[2];
        Matcher matcher = Pattern.compile("^(.*?)@(.*?)(:(\\d+))?/?(\\?(.*?))?(?:#(.*?))?$").matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid Hysteria2 URI format");
        }

        String password = matcher.group(1);
        String server = matcher.group(2);
        int port = Integer.parseInt(matcher.group(4));
        String addons = matcher.group(6);
        String name = matcher.group(7);

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("hysteria2");
        proxy.setName(getIfNotBlank(decodeURIComponent(name), "Hysteria2 " + server + ":" + port));
        proxy.setServer(server);
        proxy.setPort(port);
        proxy.setPassword(password);

        Map<String, String> params = new HashMap<>();
        if (addons != null) {
            for (String addon : addons.split("&")) {
                String[] keyValue = addon.split("=");
                params.put(keyValue[0], decodeURIComponent(keyValue[1]));
            }
        }

        proxy.setSni(params.get("sni"));
        proxy.setObfs(params.get("obfs"));
        proxy.setPorts(params.get("mport"));
        proxy.setObfsPassword(params.get("obfs-password"));
        proxy.setSkipCertVerify("1".equals(params.get("insecure")));
        proxy.setFastOpen("1".equals(params.get("fastopen")));
        proxy.setFingerprint(params.get("pinSHA256"));

        return proxy;
    }

    private static ProxyConfig uriHysteria(String line) {
        line = line.split("(hysteria|hy)://")[2];
        Matcher matcher = Pattern.compile("^(.*?)(:(\\d+))?/?(\\?(.*?))?(?:#(.*?))?$").matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid Hysteria URI format");
        }

        String server = matcher.group(1);
        int port = Integer.parseInt(matcher.group(3));
        String addons = matcher.group(5);
        String name = matcher.group(6);

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("hysteria");
        proxy.setName(getIfNotBlank(decodeURIComponent(name), "Hysteria " + server + ":" + port));
        proxy.setServer(server);
        proxy.setPort(port);

        Map<String, String> params = new HashMap<>();
        if (addons != null) {
            for (String addon : addons.split("&")) {
                String[] keyValue = addon.split("=");
                params.put(keyValue[0].replace('_', '-'), decodeURIComponent(keyValue[1]));
            }
        }

        proxy.setAlpn(params.get("alpn") != null ? Arrays.asList(params.get("alpn").split(",")) : null);
        proxy.setSkipCertVerify("1".equals(params.get("insecure")));
        proxy.setAuthStr(params.get("auth"));
        proxy.setPorts(params.get("mport"));
        proxy.setObfs(params.get("obfs"));
        proxy.setObfsPassword(params.get("obfsParam"));
        proxy.setUdp("1".equals(params.get("fast-open")));
        proxy.setFastOpen("1".equals(params.get("fast-open")));
        proxy.setSni(params.get("peer"));
        proxy.setFingerprint(params.get("fingerprint"));

        return proxy;
    }

    private static ProxyConfig uriTUIC(String line) {
        line = line.split("tuic://")[1];
        Matcher matcher = Pattern.compile("^(.*?):(.*?)@(.*?)(:(\\d+))?/?(\\?(.*?))?(?:#(.*?))?$").matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid TUIC URI format");
        }

        String uuid = matcher.group(1);
        String password = matcher.group(2);
        String server = matcher.group(3);
        int port = Integer.parseInt(matcher.group(5));
        String addons = matcher.group(7);
        String name = matcher.group(8);

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("tuic");
        proxy.setName(getIfNotBlank(decodeURIComponent(name), "TUIC " + server + ":" + port));
        proxy.setServer(server);
        proxy.setPort(port);
        proxy.setPassword(password);
        proxy.setUuid(uuid);

        Map<String, String> params = new HashMap<>();
        if (addons != null) {
            for (String addon : addons.split("&")) {
                String[] keyValue = addon.split("=");
                params.put(keyValue[0].replace('_', '-'), decodeURIComponent(keyValue[1]));
            }
        }

        proxy.setToken(params.get("token"));
        proxy.setIp(params.get("ip"));
        proxy.setHeartbeatInterval(Integer.parseInt(params.getOrDefault("heartbeat-interval", "0")));
        proxy.setAlpn(params.get("alpn") != null ? Arrays.asList(params.get("alpn").split(",")) : null);
        proxy.setDisableSni("1".equals(params.get("disable-sni")));
        proxy.setReduceRtt("1".equals(params.get("reduce-rtt")));
        proxy.setRequestTimeout(Integer.parseInt(params.getOrDefault("request-timeout", "0")));
        proxy.setUdpRelayMode(params.get("udp-relay-mode"));
        proxy.setCongestionController(params.get("congestion-controller"));
        proxy.setMaxUdpRelayPacketSize(Integer.parseInt(params.getOrDefault("max-udp-relay-packet-size", "0")));
        proxy.setFastOpen("1".equals(params.get("fast-open")));
        proxy.setSkipCertVerify("1".equals(params.get("skip-cert-verify")));
        proxy.setMaxOpenStreams(Integer.parseInt(params.getOrDefault("max-open-streams", "0")));
        proxy.setSni(params.get("sni"));

        return proxy;
    }

    private static ProxyConfig uriWireguard(String line) {
        line = line.split("(wireguard|wg)://")[2];
        Matcher matcher = Pattern.compile("^((.*?)@)?(.*?)(:(\\d+))?/?(\\?(.*?))?(?:#(.*?))?$").matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid WireGuard URI format");
        }

        String privateKey = matcher.group(3);
        String server = matcher.group(4);
        int port = Integer.parseInt(matcher.group(5));
        String addons = matcher.group(7);
        String name = matcher.group(8);

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("wireguard");
        proxy.setName(getIfNotBlank(decodeURIComponent(name), "WireGuard " + server + ":" + port));
        proxy.setServer(server);
        proxy.setPort(port);
        proxy.setPrivateKey(privateKey);

        if (addons != null) {
            for (String addon : addons.split("&")) {
                String[] keyValue = addon.split("=");
                var key = keyValue[0].replace('_', '-');
                var value = decodeURIComponent(keyValue[1]);
                switch (key) {
                    case "address":
                    case "ip":
                        Stream.of(value.split(",")).forEach(i -> {
                            var ip = i
                                    .trim()
                                    .replaceFirst("/\\d+$", "")
                                    .replaceFirst("^\\[", "")
                                    .replaceFirst("]$", "");
                            if (isIPv4(ip)) {
                                proxy.setIp(ip);
                            } else if (isIPv6(ip)) {
                                proxy.setIpv6(ip);
                            }
                        });
                        break;
                    case "publickey":
                        proxy.setPublicKey(value);
                        break;
//                    case "allowed-ips":
//                        proxy.setA["allowed-ips"] = value.split(",");
//                        break;
                    case "pre-shared-key":
                        proxy.setPreSharedKey(value);
                        break;
                    case "reserved":
                        proxy.setReserved(value);
                        break;
                    case "udp":
                        proxy.setUdp(value.toUpperCase().matches("(TRUE)|1"));
                        break;
                    case "mtu":
                        proxy.setMtu(Integer.parseInt(value.trim(), 10));
                        break;
                    case "dialer-proxy":
                        proxy.setDialerProxy(value);
                        break;
                    case "remote-dns-resolve":
                        proxy.setRemoteDnsResolve(value != null && value.toUpperCase().matches("(TRUE)|1"));
                        break;
                    case "dns":
                        proxy.setDns(List.of(value.split(",")));
                        break;
                    default:
                        break;
                }
            }
        }

        return proxy;
    }

    private static ProxyConfig uriHTTP(String line) {
        line = line.split("(http|https)://")[2];
        Matcher matcher = Pattern.compile("^((.*?)@)?(.*?)(:(\\d+))?/?(\\?(.*?))?(?:#(.*?))?$").matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid HTTP URI format");
        }

        String auth = matcher.group(2);
        String server = matcher.group(3);
        int port = Integer.parseInt(matcher.group(5));
        String addons = matcher.group(7);
        String name = matcher.group(8);

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("http");
        proxy.setName(getIfNotBlank(decodeURIComponent(name), "HTTP " + server + ":" + port));
        proxy.setServer(server);
        proxy.setPort(port);

        if (auth != null) {
            String[] credentials = auth.split(":");
            proxy.setUsername(credentials[0]);
            proxy.setPassword(credentials[1]);
        }

        Map<String, String> params = new HashMap<>();
        if (addons != null) {
            for (String addon : addons.split("&")) {
                String[] keyValue = addon.split("=");
                params.put(keyValue[0].replace('_', '-'), decodeURIComponent(keyValue[1]));
            }
        }

        proxy.setTls("1".equals(params.get("tls")));
        proxy.setFingerprint(params.get("fingerprint"));
        proxy.setSkipCertVerify("1".equals(params.get("skip-cert-verify")));
        proxy.setIpVersion(params.getOrDefault("ip-version", "dual"));

        return proxy;
    }

    private static ProxyConfig uriSOCKS(String line) {
        line = line.split("socks5://")[1];
        Matcher matcher = Pattern.compile("^((.*?)@)?(.*?)(:(\\d+))?/?(\\?(.*?))?(?:#(.*?))?$").matcher(line);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid SOCKS5 URI format");
        }

        String auth = matcher.group(2);
        String server = matcher.group(3);
        int port = Integer.parseInt(matcher.group(5));
        String addons = matcher.group(7);
        String name = matcher.group(8);

        ProxyConfig proxy = new ProxyConfig();
        proxy.setType("socks5");
        proxy.setName(getIfNotBlank(decodeURIComponent(name), "SOCKS5 " + server + ":" + port));
        proxy.setServer(server);
        proxy.setPort(port);

        if (auth != null) {
            String[] credentials = auth.split(":");
            proxy.setUsername(credentials[0]);
            proxy.setPassword(credentials[1]);
        }

        Map<String, String> params = new HashMap<>();
        if (addons != null) {
            for (String addon : addons.split("&")) {
                String[] keyValue = addon.split("=");
                params.put(keyValue[0].replace('_', '-'), decodeURIComponent(keyValue[1]));
            }
        }

        proxy.setTls("1".equals(params.get("tls")));
        proxy.setFingerprint(params.get("fingerprint"));
        proxy.setSkipCertVerify("1".equals(params.get("skip-cert-verify")));
        proxy.setUdp("1".equals(params.get("udp")));
        proxy.setIpVersion(params.getOrDefault("ip-version", "dual"));

        return proxy;
    }

    private static String decodeURIComponent(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private static String atob(String value) {
        return new String(Base64.getDecoder().decode(value));
    }
}