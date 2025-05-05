package cn.bincker.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class TwoFactorAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final AntPathRequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = new AntPathRequestMatcher("/auth/sign-in-2fa",
            "POST");

    public TwoFactorAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(DEFAULT_ANT_PATH_REQUEST_MATCHER, authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        var twoFactorAuthentication = getAuthentication();
        var details = twoFactorAuthentication.getPrevAuthentication().getDetails();
        if (details == null) throw new AuthenticationServiceException("Two-factor authentication failed");
        if (!TwoFactorAuthenticationDetailsSource.TwoFactorAuthenticationDetails.class.isAssignableFrom(details.getClass()))
            throw new AuthenticationServiceException("Two-factor authentication failed");
        if ((System.currentTimeMillis() - ((TwoFactorAuthenticationDetailsSource.TwoFactorAuthenticationDetails) details).getAuthenticationTime()) > 5 * 60 * 1000L){
            SecurityContextHolder.getContext().setAuthentication(null);
            throw new AuthenticationServiceException("authentication timeout");
        }
        var code = getTwoFactorCode(request);

        var authRequest = TwoFactorAuthenticationToken.unauthenticated(twoFactorAuthentication.getPrevAuthentication(), code);
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    private int getTwoFactorCode(HttpServletRequest request) {
        var strCode = request.getParameter("code");
        if (strCode == null || !strCode.matches("^\\d{6}$")){
            throw new AuthenticationServiceException("Two-factor authentication code is incorrect");
        }
        return Integer.parseInt(strCode);
    }

    private static @NotNull TwoFactorAuthenticationToken getAuthentication() {
        var twoFactorAuthentication = SecurityContextHolder.getContext().getAuthentication();
        if (twoFactorAuthentication == null)
            throw new AuthenticationServiceException("prev Authentication required");
        if (!TwoFactorAuthenticationToken.class.isAssignableFrom(twoFactorAuthentication.getClass()))
            throw new AuthenticationServiceException("prev Authentication type error");
        if (twoFactorAuthentication.isAuthenticated())
            throw new AuthenticationServiceException("prev Authentication already authenticated");
        return (TwoFactorAuthenticationToken) twoFactorAuthentication;
    }

}
