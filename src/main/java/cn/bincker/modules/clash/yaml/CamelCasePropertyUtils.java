package cn.bincker.modules.clash.yaml;

import cn.bincker.common.utils.NameUtils;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class CamelCasePropertyUtils extends PropertyUtils {
    public CamelCasePropertyUtils() {
        setSkipMissingProperties(true);
        //保证顺序相同
        setBeanAccess(BeanAccess.FIELD);
    }

    @Override
    public Property getProperty(Class<?> type, String name) {
        if (name.contains("-")){
            name = NameUtils.kebabCaseToCamelCase(name);
        }
        return super.getProperty(type, name);
    }

    @Override
    protected Set<Property> createPropertySet(Class<?> type, BeanAccess bAccess) {
        Set<Property> properties = new LinkedHashSet<>();
        Collection<Property> props = getPropertiesMap(type, bAccess).values();
        for (Property property : props) {
            if (property.isReadable() && (isAllowReadOnlyProperties() || property.isWritable())) {
                properties.add(property);
            }
        }
        return properties;
    }
}
