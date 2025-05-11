package cn.bincker.modules.clash.entity;

import cn.bincker.modules.clash.entity.config.ClashConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ClashConfigTest {

    @Test
    public void testConfigSerialization() throws IOException {
        // 读取原始配置文件
        String originalYaml = Files.lines(Paths.get("src/test/resources/clash-config-test.yaml"))
                .filter(line -> !line.trim().startsWith("#")) // 过滤掉注释行
                .collect(Collectors.joining("\n"));

        // 创建YAML解析器
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        yamlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        yamlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
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

    @Test
    void snakeTest() throws IOException {
        var options = new LoaderOptions();
        var constructor = new Constructor(ClashConfig.class, options);
        constructor.setPropertyUtils(new CustomPropertyUtils());
        var yaml = new Yaml(constructor);
        var config = yaml.load(new FileInputStream("src/test/resources/clash-config-test.yaml"));

        var dumpOptions = new DumperOptions();
        var representer = new Representer(dumpOptions){
            public void setNullRepresent(Represent representer) {
                nullRepresenter = representer;
            }
        };
//        representer.setNullRepresent();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        representer.getPropertyUtils().setSkipMissingProperties(false);
        dumpOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        var serialYaml = new Yaml(representer);
        System.out.println(serialYaml.dump(config));
    }

    public static class CustomPropertyUtils extends PropertyUtils {
        public CustomPropertyUtils() {
            setSkipMissingProperties(true);
        }

        @Override
        public Property getProperty(Class<?> type, String name) {

            // 如果找不到，尝试将连字符转换为驼峰命名
            if (name.contains("-")) {
                name = convertKebabCaseToCamelCase(name);
            }

            return super.getProperty(type, name);
        }

        private String convertKebabCaseToCamelCase(String kebabCase) {
            StringBuilder camelCase = new StringBuilder();
            boolean nextUpper = false;

            for (int i = 0; i < kebabCase.length(); i++) {
                char currentChar = kebabCase.charAt(i);

                if (currentChar == '-') {
                    nextUpper = true;
                } else {
                    if (nextUpper) {
                        camelCase.append(Character.toUpperCase(currentChar));
                        nextUpper = false;
                    } else {
                        camelCase.append(currentChar);
                    }
                }
            }

            return camelCase.toString();
        }
    }
}