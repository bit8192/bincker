package cn.bincker.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

public class TwoFactorAuthenticationDetailsSource extends WebAuthenticationDetailsSource {
    @Override
    public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new TwoFactorAuthenticationDetails(context);
    }

    public static class TwoFactorAuthenticationDetails extends WebAuthenticationDetails {
        @Getter
        private final long authenticationTime;
        public TwoFactorAuthenticationDetails(HttpServletRequest request) {
            super(request);
            this.authenticationTime = System.currentTimeMillis();
        }
    }
}
