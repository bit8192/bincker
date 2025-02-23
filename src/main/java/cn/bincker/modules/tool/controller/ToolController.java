package cn.bincker.modules.tool.controller;

import cn.bincker.modules.tool.service.ToolService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("tool")
public class ToolController {
    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @GetMapping
    public String getTools(Model model) {
        model.addAttribute("tools", toolService.getAll());
        return "tool/index";
    }

    @GetMapping("/**")
    public String random(HttpServletRequest request) {
        var path = request.getRequestURI().replaceFirst("^/", "");
        if (toolService.exists(path)) {
            return path;
        }
        return "redirect:404";
    }
}
