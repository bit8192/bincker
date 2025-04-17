package cn.bincker.modules.todo.entity;

import lombok.Data;

@Data
public class TodoStatisticVo {
    private Long total = 0L;
    private Long completed = 0L;
    private Long cancelled = 0L;
    private Long procrastination = 0L;
    private Long miss = 0L;
}
