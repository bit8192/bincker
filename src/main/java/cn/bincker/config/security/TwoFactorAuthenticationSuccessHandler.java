package cn.bincker.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

@Slf4j
public class TwoFactorAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private final TwoFactorAuthenticationRememberMeService rememberMeService;

    public TwoFactorAuthenticationSuccessHandler(TwoFactorAuthenticationRememberMeService rememberMeService) {
        this.rememberMeService = rememberMeService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (TwoFactorAuthenticationToken.class.isAssignableFrom(authentication.getClass())) {
            if (!authentication.isAuthenticated()){
                getRedirectStrategy().sendRedirect(request, response, "/auth/sign-in-2fa" + (rememberMeService.rememberMeRequested(request, rememberMeService.getParameter()) ? "?" + rememberMeService.getParameter() : ""));
                clearAuthenticationAttributes(request);
            }else{
                super.onAuthenticationSuccess(request, response, authentication);
            }
        }else throw new BadCredentialsException("unknown authentication");
    }
}
