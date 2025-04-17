package cn.bincker.modules.todo.entity;

import cn.bincker.common.DDL;
import cn.bincker.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "todo_log", autoResultMap = true)
@DDL("""
create table if not exists todo_log(
        id unsigned bigint(20) primary key,
        created_time timestamp default current_timestamp,
        updated_time timestamp default current_timestamp,
        deleted tinyint(1) default 0,
        todo_id unsigned bigint(20) not null,
        prev_status tinyint(1) not null,
        to_status tinyint(1) not null,
        content text
)
""")
public class TodoLog extends BaseEntity {
    private Long todoId;
    private Todo.Status prevStatus;
    private Todo.Status toStatus;
    private String content;
}
