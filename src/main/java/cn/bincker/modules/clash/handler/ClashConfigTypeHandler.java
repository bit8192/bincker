package cn.bincker.modules.clash.handler;

import cn.bincker.modules.clash.entity.config.ClashConfig;
import cn.bincker.modules.clash.yaml.CamelCasePropertyUtils;
import cn.bincker.modules.clash.yaml.NoNullRepresenter;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClashConfigTypeHandler extends BaseTypeHandler<ClashConfig> {
    private static final Yaml YAML;
    static {
        var loadOptions = new LoaderOptions();
        var constructor = new Constructor(ClashConfig.class, loadOptions);
        constructor.addTypeDescription(new TypeDescription(ClashConfig.class));
        constructor.setPropertyUtils(new CamelCasePropertyUtils());
        var dumpOptions = new DumperOptions();
        dumpOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        var representer = new NoNullRepresenter(dumpOptions);
        representer.addClassTag(ClashConfig.class, Tag.MAP);
        YAML = new Yaml(constructor, representer, dumpOptions);
    }

    public static Yaml getClashConfigYaml() {
        return YAML;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ClashConfig parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, getClashConfigYaml().dump(parameter));
    }

    @Override
    public ClashConfig getNullableResult(ResultSet rs, String columnName) throws SQLException {
        var strContent = rs.getString(columnName);
        if (strContent == null || strContent.isBlank()) return null;
        return getClashConfigYaml().load(new ByteArrayInputStream(strContent.getBytes(StandardCharsets.UTF_16)));
    }

    @Override
    public ClashConfig getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        var strContent = rs.getString(columnIndex);
        if (strContent == null || strContent.isBlank()) return null;
        return getClashConfigYaml().load(new ByteArrayInputStream(strContent.getBytes(StandardCharsets.UTF_16)));
    }

    @Override
    public ClashConfig getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        var strContent = cs.getString(columnIndex);
        if (strContent == null || strContent.isBlank()) return null;
        return getClashConfigYaml().load(new ByteArrayInputStream(strContent.getBytes(StandardCharsets.UTF_16)));
    }
}
