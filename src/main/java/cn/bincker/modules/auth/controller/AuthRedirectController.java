package cn.bincker.modules.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthRedirectController {
    @GetMapping({"login", "sign-in", "signin"})
    public String signIn() {
        return "redirect:/auth/sign-in";
    }

    @GetMapping({"logout", "sign-out", "signout"})
    public String signOut() {
        return "redirect:/auth/sign-out";
    }
}
