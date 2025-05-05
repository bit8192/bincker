package cn.bincker.modules.auth.controller;

import cn.bincker.config.security.TwoFactorAuthenticationDetailsSource;
import cn.bincker.config.security.TwoFactorAuthenticationToken;
import cn.bincker.modules.auth.dto.UserSignUpDto;
import cn.bincker.modules.auth.service.impl.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("auth")
@Slf4j
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("sign-up")
    public String signUp() {
        if (userService.countAll() > 0) return "redirect:/auth/sign-in";
        return "auth/sign-up";
    }

    @PostMapping("sign-up")
    public String doSignUp(UserSignUpDto userSignUpDto, Model model) {
        if (userService.countAll() > 0) return "redirect:/auth/sign-in";
        var user = userService.signUp(userSignUpDto);
        model.addAttribute("user", user);
        model.addAttribute("TFA_QRCODE", userService.getUser2FAQRCodeUrl(user));
        return "auth/sign-up-completed";
    }

    @GetMapping({"sign-in", "sign-in-2fa"})
    public String signIn(HttpServletRequest request, Model model) {
        if (userService.countAll() < 1) return "redirect:/auth/sign-up";
        var step = request.getServletPath().endsWith("sign-in-2fa") ? 2 : 1;
        log.debug("step:{}, sessionId: {}", step, request.getSession().getId());
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (step == 1){
            if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken)) {
                if (authentication.isAuthenticated()){
                    return "redirect:/";
                }else{
                    if (authentication instanceof TwoFactorAuthenticationToken){
                        var prevAuthentication = ((TwoFactorAuthenticationToken) authentication).getPrevAuthentication();
                        if (prevAuthentication != null) {
                            var detail = prevAuthentication.getDetails();
                            if (detail instanceof TwoFactorAuthenticationDetailsSource.TwoFactorAuthenticationDetails){
                                if ((System.currentTimeMillis() - ((TwoFactorAuthenticationDetailsSource.TwoFactorAuthenticationDetails) detail).getAuthenticationTime()) < 5 * 60 * 1000) {
                                    return "redirect:/auth/sign-in-2fa";
                                }
                            }
                        }
                    }
                    SecurityContextHolder.getContextHolderStrategy().clearContext();
                }
            }
        }else {
            if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
                return "redirect:/auth/sign-in";
            }
        }
        model.addAttribute("step", step);
        return "auth/sign-in";
    }
}
