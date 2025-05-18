package cn.bincker.system.database;

import cn.bincker.common.annotation.DDL;
import cn.bincker.common.utils.NameUtils;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.Mapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Slf4j
public class DatabaseSchemaSynchronizer implements ApplicationListener<ApplicationReadyEvent> {
    private static final Pattern REGEXP_SQL_TABLE_NAME = Pattern.compile(
            "create table (?:if not exists )?([a-zA-Z_]+)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern REGEXP_SQL_FIELDS = Pattern.compile(
            "create table [^(]+\\(([\\S\\s]+)\\);?",
            Pattern.CASE_INSENSITIVE & Pattern.MULTILINE
    );
    private static final Pattern REGEXP_SQL_FIELD = Pattern.compile(
            "([a-zA-Z_]+)\\s+(?:\\w+\\s+)*[a-zA-Z]+(?:\\(\\s*\\d+(?:\\s*\\d+\\s*,\\s*)*\\))?(?:\\s*\\w+\\s*)*",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void onApplicationEvent(@NotNull ApplicationReadyEvent event) {
        var application = event.getApplicationContext();
        var mappers = application.getBeansOfType(Mapper.class);

        try(var connection = application.getBean(DataSource.class).getConnection()) {
            connection.setAutoCommit(true);
            var tables = selectTables(connection);
            for (var mapper : mappers.values()) {
                var entityType = ReflectionKit.getSuperClassGenericType(mapper.getClass(), Mapper.class, 0);
                var ddlAnnotation = entityType.getAnnotation(DDL.class);
                if (ddlAnnotation == null) continue;
                var tableNameAnnotation = entityType.getAnnotation(TableName.class);
                String tableName;
                if (tableNameAnnotation != null) {
                    tableName = tableNameAnnotation.value();
                } else {
                    var matcher = REGEXP_SQL_TABLE_NAME.matcher(ddlAnnotation.value());
                    if (matcher.find()) {
                        tableName = matcher.group(1);
                    } else {
                        tableName = NameUtils.camelCaseToSnakeCase(entityType.getSimpleName());
                    }
                }
                var tableInfo = tables.stream().filter(t->tableName.equalsIgnoreCase(t.getTableName())).findFirst().orElse(null);
                if (tableInfo == null){
                    log.debug("create table: {}", tableName);
                    try(var statement = connection.createStatement()){
                        statement.execute(ddlAnnotation.value());
                    }
                    continue;
                }
                var columnsMatcher = REGEXP_SQL_FIELDS.matcher(ddlAnnotation.value());
                if (!columnsMatcher.find()) {
                    log.warn("table ddl not match [{}]", entityType.getName());
                    continue;
                }
                columnsMatcher = REGEXP_SQL_FIELD.matcher(columnsMatcher.group(1));
                while (columnsMatcher.find()) {
                    var columnDefinition = columnsMatcher.group();
                    var columnName = columnsMatcher.group(1);
                    if (tableInfo.columns.stream().noneMatch(columnName::equalsIgnoreCase)){
                        try(var statement = connection.createStatement()){
                            var addColumnSql = "alter table " + tableName + " add column " + columnDefinition;
                            log.debug("add column sql:{}", addColumnSql);
                            statement.execute(addColumnSql);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TableInfo> selectTables(Connection connection) throws SQLException {
        var metaData = connection.getMetaData();
        var resultSet = metaData.getTables(null, null, "%", new String[]{"TABLE"});
        List<TableInfo> tables = new ArrayList<>();
        while (resultSet.next()) {
            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableName(resultSet.getString("TABLE_NAME"));
            var columnResultSet = metaData.getColumns(null, null, tableInfo.getTableName(), null);
            var columns = new ArrayList<String>();
            while (columnResultSet.next()) {
                columns.add(columnResultSet.getString("COLUMN_NAME"));
            }
            tableInfo.setColumns(columns);
            tables.add(tableInfo);
        }
        return tables;
    }

    @Data
    private static class TableInfo {
        private String tableName;
        private List<String> columns;
    }
}
