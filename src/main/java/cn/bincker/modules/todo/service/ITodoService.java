package cn.bincker.modules.todo.service;

import cn.bincker.modules.todo.entity.Todo;
import cn.bincker.modules.todo.entity.TodoStatisticVo;
import cn.bincker.modules.todo.entity.UpdateTodoStatusDto;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

public interface ITodoService {
    /**
     * 统计
     */
    TodoStatisticVo statistic();
    /**
     * 分页查询
     */
    Page<Todo> getPage(PageDTO<Todo> dto);

    /**
     * 添加
     */
    Todo addTodo(Todo todo);

    /**
     * 改变状态
     */
    Todo updateStatus(UpdateTodoStatusDto dto);
}
