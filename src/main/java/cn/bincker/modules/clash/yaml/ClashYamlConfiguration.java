package cn.bincker.modules.clash.yaml;

import cn.bincker.modules.clash.entity.config.ClashConfig;
import cn.bincker.modules.clash.handler.ClashConfigTypeHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

@Configuration
public class ClashYamlConfiguration {
    @Bean
    public Yaml clashConfigYaml(){
        var loadOptions = new LoaderOptions();
        var constructor = new Constructor(ClashConfig.class, loadOptions);
        constructor.addTypeDescription(new TypeDescription(ClashConfig.class));
        constructor.setPropertyUtils(new CamelCasePropertyUtils());
        var dumpOptions = new DumperOptions();
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        var representer = new NoNullRepresenter(dumpOptions);
        representer.addClassTag(ClashConfig.class, Tag.MAP);
        var yaml = new Yaml(constructor, representer, dumpOptions);
        ClashConfigTypeHandler.setClashConfigYaml(yaml);
        return yaml;
    }
}
