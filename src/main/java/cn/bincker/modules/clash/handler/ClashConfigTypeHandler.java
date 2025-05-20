package cn.bincker.modules.clash.handler;

import cn.bincker.modules.clash.entity.config.ClashConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class ClashConfigTypeHandler extends BaseTypeHandler<ClashConfig> {
    @Getter
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ClashConfig parameter, JdbcType jdbcType) throws SQLException {
        try {
            ps.setString(i, getOBJECT_MAPPER().writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            log.error("write clash config failed.", e);
        }
    }

    @Override
    public ClashConfig getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return readClashConfig(rs.getBytes(columnName));
    }

    @Override
    public ClashConfig getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return readClashConfig(rs.getBytes(columnIndex));
    }

    @Override
    public ClashConfig getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return readClashConfig(cs.getBytes(columnIndex));
    }

    private static @Nullable ClashConfig readClashConfig(byte[] rs) {
        if (rs == null) return null;
        try {
            return getOBJECT_MAPPER().readValue(new InputStreamReader(new ByteArrayInputStream(rs), StandardCharsets.UTF_8), ClashConfig.class);
        } catch (IOException e) {
            log.error("read clash config error.", e);
            return null;
        }
    }
}
