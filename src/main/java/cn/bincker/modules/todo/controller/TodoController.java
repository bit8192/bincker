package cn.bincker.modules.todo.controller;

import cn.bincker.modules.todo.entity.Todo;
import cn.bincker.modules.todo.service.ITodoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("todo")
public class TodoController {
    private final ITodoService todoService;

    public TodoController(ITodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public String index(Model model, PageDTO<Todo> page) {
        model.addAttribute("statistic", todoService.statistic());
        model.addAttribute("list", todoService.getPage(page));
        return "todo/index";
    }
}
