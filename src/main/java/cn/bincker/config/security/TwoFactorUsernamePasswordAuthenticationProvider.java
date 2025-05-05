package cn.bincker.config.security;

import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public class TwoFactorUsernamePasswordAuthenticationProvider extends DaoAuthenticationProvider {
    @Override
    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        return TwoFactorAuthenticationToken.unauthenticated(
                super.createSuccessAuthentication(principal, authentication, user),
                0
        );
    }
}
