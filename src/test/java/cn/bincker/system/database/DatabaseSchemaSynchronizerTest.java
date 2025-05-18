package cn.bincker.system.database;

import cn.bincker.common.annotation.DDL;
import cn.bincker.modules.todo.entity.Todo;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseSchemaSynchronizerTest {
    private static final Pattern REGEXP_SQL_FIELDS = Pattern.compile(
            "create table [^(]+\\(([\\S\\s]+)\\);?",
            Pattern.CASE_INSENSITIVE & Pattern.MULTILINE
    );
    private static final Pattern REGEXP_SQL_FIELD = Pattern.compile(
            "([a-zA-Z_]+)\\s+(?:\\w+\\s+)*[a-zA-Z]+(?:\\(\\s*\\d+(?:\\s*\\d+\\s*,\\s*)*\\))?(?:\\s*\\w+\\s*)*",
            Pattern.CASE_INSENSITIVE
    );

    @Test
    void regexTest() {
        var ddl = Objects.requireNonNull(Todo.class.getAnnotation(DDL.class)).value();
        var matcher = REGEXP_SQL_FIELDS.matcher(ddl);
        assertTrue(matcher.find());
        var fields = matcher.group(1);
        matcher = REGEXP_SQL_FIELD.matcher(fields);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}