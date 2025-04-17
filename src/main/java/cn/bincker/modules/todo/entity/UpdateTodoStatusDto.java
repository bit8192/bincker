package cn.bincker.modules.todo.entity;

import lombok.Data;

@Data
public class UpdateTodoStatusDto {
    private Long id;
    private Todo.Status status;
    private String content;
}
