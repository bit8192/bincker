package cn.bincker.modules.clash.entity.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;
import java.util.List;

@Data
@JsonDeserialize(using = TunnelConfig.TunnelConfigDeserializer.class)
public class TunnelConfig {
    private List<String> network;
    private String address;
    private String target;
    private String proxy;

    public static class TunnelConfigDeserializer extends JsonDeserializer<TunnelConfig> {
        @Override
        public TunnelConfig deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            TunnelConfig config = new TunnelConfig();

            // 如果是字符串，说明是单行格式
            if (node.isTextual()) {
                String[] parts = node.asText().split(",");
                if (parts.length >= 4) {
                    // 解析网络类型
                    String[] networks = parts[0].split("/");
                    config.setNetwork(List.of(networks));
                    config.setAddress(parts[1]);
                    config.setTarget(parts[2]);
                    config.setProxy(parts[3]);
                }
            } 
            // 如果是对象，说明是完整YAML格式
            else if (node.isObject()) {
                if (node.has("network")) {
                    config.setNetwork(node.get("network").findValuesAsText("network"));
                }
                if (node.has("address")) {
                    config.setAddress(node.get("address").asText());
                }
                if (node.has("target")) {
                    config.setTarget(node.get("target").asText());
                }
                if (node.has("proxy")) {
                    config.setProxy(node.get("proxy").asText());
                }
            }

            return config;
        }
    }
} 