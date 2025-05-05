package cn.bincker.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class TwoFactorAuthenticationRememberMeService extends TokenBasedRememberMeServices {
    private static final Path KEY_FILE = Path.of("remember-me.key");
    private static String readKey() {
        if (!Files.exists(KEY_FILE)) {
            try {
                Files.writeString(KEY_FILE, generateKey());
            } catch (IOException e) {
                throw new RuntimeException("write remember me key failed.", e);
            }
        }
        try {
            return Files.readString(KEY_FILE);
        } catch (IOException e) {
            throw new RuntimeException("read remember me key failed.", e);
        }
    }

    private static String generateKey() {
        var random = new SecureRandom();
        random.setSeed(System.currentTimeMillis());
        return Long.toString(Math.abs(random.nextLong()), 36) + Long.toString(Math.abs(random.nextLong()), 36);
    }

    public TwoFactorAuthenticationRememberMeService(UserDetailsService userDetailsService) {
        super(readKey(), userDetailsService);
    }

    @Override
    public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        if (!successfulAuthentication.isAuthenticated()) return;
        super.onLoginSuccess(request, response, successfulAuthentication);
    }

    @Override
    public boolean rememberMeRequested(HttpServletRequest request, String parameter) {
        return super.rememberMeRequested(request, parameter);
    }
}
