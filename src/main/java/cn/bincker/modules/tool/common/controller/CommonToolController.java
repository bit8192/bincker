package cn.bincker.modules.tool.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("tool/common")
public class CommonToolController {
    @GetMapping
    public String index() {
        return "tool/common/index";
    }

    @GetMapping("/*")
    public String random(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
