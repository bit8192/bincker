package cn.bincker.modules.todo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("todo")
public class TodoController {
//    private ITodoService
    public String index(Model model) {
        return "todo/index";
    }
}
