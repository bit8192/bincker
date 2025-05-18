package cn.bincker.modules.clash.yaml;

import cn.bincker.common.utils.NameUtils;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.FieldProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CamelCasePropertyUtils extends PropertyUtils {
    private final Map<Class<?>, Map<String, Property>> propertiesCache = new HashMap<>();

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
    protected Map<String, Property> getPropertiesMap(Class<?> type, BeanAccess bAccess) {
        if (propertiesCache.containsKey(type)) {
            return propertiesCache.get(type);
        }

        Map<String, Property> properties = new LinkedHashMap<String, Property>();
        if (bAccess == BeanAccess.FIELD) {
            for (Class<?> c = type; c != null; c = c.getSuperclass()) {
                for (Field field : c.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)
                            && !properties.containsKey(field.getName())) {
                        properties.put(field.getName(), new FieldProperty(field));
                    }
                }
            }
        } else {// add JavaBean properties
            return super.getPropertiesMap(type, bAccess);
        }
        propertiesCache.put(type, properties);
        return properties;
    }


}
