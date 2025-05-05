package cn.bincker.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class TwoFactorAuthenticationToken implements Authentication, CredentialsContainer {
    @Getter
    private final Authentication prevAuthentication;
    private final boolean authenticated;
    @Setter
    private Object details;

    @Getter
    private int twoFactorCode;
    public TwoFactorAuthenticationToken(Authentication prevAuthentication, int twoFactorCode, boolean authenticated) {
        this.prevAuthentication = prevAuthentication;
        this.twoFactorCode = twoFactorCode;
        this.authenticated = authenticated;
    }

    public static TwoFactorAuthenticationToken unauthenticated(Authentication prevAuthentication, int twoFactorCode) {
        return new TwoFactorAuthenticationToken(prevAuthentication, twoFactorCode, false);
    }

    public static TwoFactorAuthenticationToken authenticated(Authentication prevAuthentication, int twoFactorCode) {
        return new TwoFactorAuthenticationToken(prevAuthentication, twoFactorCode, true);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return prevAuthentication.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return prevAuthentication.getCredentials();
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return prevAuthentication.getPrincipal();
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        prevAuthentication.setAuthenticated(isAuthenticated);
    }

    @Override
    public String getName() {
        return prevAuthentication.getName();
    }

    @Override
    public void eraseCredentials() {
        if (prevAuthentication instanceof CredentialsContainer) {
            ((CredentialsContainer) prevAuthentication).eraseCredentials();
        }
        twoFactorCode = 0;
    }
}
