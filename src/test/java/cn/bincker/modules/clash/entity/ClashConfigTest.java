package cn.bincker.modules.clash.entity;

import cn.bincker.modules.clash.entity.config.ClashConfig;
import cn.bincker.modules.clash.handler.ClashConfigTypeHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@SpringBootTest
public class ClashConfigTest {

    @Test
    public void testConfigSerialization() throws IOException {
        // 读取原始配置文件
        String originalYaml;
        try(var lines = Files.lines(Paths.get("src/test/resources/clash-config-test.yaml"))) {
            originalYaml = lines
                    .filter(line -> !line.trim().startsWith("#")) // 过滤掉注释行
                    .collect(Collectors.joining("\n"));
        }

        var objectMapper = ClashConfigTypeHandler.getOBJECT_MAPPER();

        // 将YAML解析为ClashConfig对象
        ClashConfig config = objectMapper.readValue(new ByteArrayInputStream(originalYaml.getBytes(StandardCharsets.UTF_8)), ClashConfig.class);
        
        // 将ClashConfig对象序列化回YAML
        String serializedYaml = objectMapper.writeValueAsString(config);

        // 比较两个对象是否相等
//        assertEquals(config, reParsedConfig);
        
        // 打印序列化后的YAML，用于调试
        System.out.println("Serialized YAML:");
        System.out.println(serializedYaml);
    }
}