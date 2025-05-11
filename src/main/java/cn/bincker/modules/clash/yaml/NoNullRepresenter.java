package cn.bincker.modules.clash.yaml;

import cn.bincker.common.utils.KebabCaseUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

public class NoNullRepresenter extends Representer {
    public NoNullRepresenter(DumperOptions options) {
        super(options);
    }

    @Override
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
        var raw = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        if (raw.getValueNode().getTag().equals(Tag.NULL)){
            return null;
        }
        return new NodeTuple(representData(KebabCaseUtils.camelCaseToKebabCase(property.getName())), raw.getValueNode());
    }
}
