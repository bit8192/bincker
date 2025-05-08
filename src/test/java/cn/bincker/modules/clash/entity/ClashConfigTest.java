package cn.bincker.modules.clash.entity;

import cn.bincker.modules.clash.entity.config.ClashConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ClashConfigTest {

    @Test
    public void testConfigSerialization() throws IOException {
        // 读取原始配置文件
        String originalYaml = Files.lines(Paths.get("src/test/resources/clash-config.yaml"))
                .filter(line -> !line.trim().startsWith("#")) // 过滤掉注释行
                .collect(Collectors.joining("\n"));

        // 创建YAML解析器
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 将YAML解析为ClashConfig对象
        ClashConfig config = yamlMapper.readValue(originalYaml, ClashConfig.class);
        
        // 将ClashConfig对象序列化回YAML
        String serializedYaml = yamlMapper.writeValueAsString(config);

        // 比较两个对象是否相等
//        assertEquals(config, reParsedConfig);
        
        // 打印序列化后的YAML，用于调试
        System.out.println("Serialized YAML:");
        System.out.println(serializedYaml);
    }
} 