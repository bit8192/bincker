package cn.bincker.modules.clash.handler;

import cn.bincker.modules.clash.entity.config.ClashConfig;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClashConfigTypeHandler extends BaseTypeHandler<ClashConfig> {
    private static Yaml YAML;

    private static Yaml getClashConfigYaml() {
        return YAML;
    }

    public static void setClashConfigYaml(Yaml yaml) {
        YAML = yaml;
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
