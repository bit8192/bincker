package cn.bincker.modules.clash.entity;

import cn.bincker.modules.clash.entity.config.ClashConfig;
import cn.bincker.modules.clash.yaml.CamelCasePropertyUtils;
import cn.bincker.modules.clash.yaml.NoNullRepresenter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@SpringBootTest
public class ClashConfigTest {

    @Test
    public void testConfigSerialization() throws IOException {
        // 读取原始配置文件
        String originalYaml = Files.lines(Paths.get("src/test/resources/clash-config.yaml"))
                .filter(line -> !line.trim().startsWith("#")) // 过滤掉注释行
                .collect(Collectors.joining("\n"));

        // 创建YAML解析器
        var loadOptions = new LoaderOptions();
        var constructor = new Constructor(ClashConfig.class, loadOptions);
        constructor.addTypeDescription(new TypeDescription(ClashConfig.class));
        constructor.setPropertyUtils(new CamelCasePropertyUtils());
        var dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        var representer = new NoNullRepresenter(dumperOptions);
        representer.addClassTag(ClashConfig.class, Tag.MAP);
        var loadYaml = new Yaml(constructor, representer, dumperOptions);
//        var dumpYaml = new Yaml(representer);

        // 将YAML解析为ClashConfig对象
        ClashConfig config = loadYaml.load(originalYaml);
        
        // 将ClashConfig对象序列化回YAML
        String serializedYaml = loadYaml.dump(config);

        // 比较两个对象是否相等
//        assertEquals(config, reParsedConfig);
        
        // 打印序列化后的YAML，用于调试
        System.out.println("Serialized YAML:");
        System.out.println(serializedYaml);
    }
}