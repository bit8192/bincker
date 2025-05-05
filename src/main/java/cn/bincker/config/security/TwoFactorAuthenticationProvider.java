package cn.bincker.config.security;

import cn.bincker.modules.auth.service.impl.UserService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class TwoFactorAuthenticationProvider implements AuthenticationProvider {
    private final UserService userService;

    public TwoFactorAuthenticationProvider(UserService userService) {
        super();
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var twoAuth = (TwoFactorAuthenticationToken) authentication;
        if (!userService.verify2FACode(twoAuth.getName(), twoAuth.getTwoFactorCode())){
            throw new BadCredentialsException("Two Factor Authentication Failed");
        }
        var result = TwoFactorAuthenticationToken.authenticated(twoAuth.getPrevAuthentication(), twoAuth.getTwoFactorCode());
        result.setDetails(twoAuth.getDetails());
        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TwoFactorAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
