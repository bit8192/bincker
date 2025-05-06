package cn.bincker.modules.mihomo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("mihomo")
public class MihomoController {
    @GetMapping
    public String index() {
        return "mihomo/index";
    }
}
