package cn.bincker.modules.clash.yaml;

import cn.bincker.common.utils.KebabCaseUtils;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class CamelCasePropertyUtils extends PropertyUtils {
    public CamelCasePropertyUtils() {
        setSkipMissingProperties(true);
    }

    @Override
    public Property getProperty(Class<?> type, String name) {
        if (name.contains("-")){
            name = KebabCaseUtils.kebabCaseToCamelCase(name);
        }
        return super.getProperty(type, name);
    }
}
