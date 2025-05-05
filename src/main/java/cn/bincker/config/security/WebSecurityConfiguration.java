package cn.bincker.config.security;

import cn.bincker.modules.auth.service.impl.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@Slf4j
public class WebSecurityConfiguration {
    private final UserService userDetailsService;

    public WebSecurityConfiguration(@Lazy UserService userService) {
        this.userDetailsService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        return http
                .authorizeHttpRequests(authorize->
                                authorize
                                        .requestMatchers("/todo/**").authenticated()
                                        .requestMatchers("/auth/**").permitAll()
                                        .anyRequest().permitAll()
                )
                .userDetailsService(userDetailsService)
                .formLogin(formLogin->{
                    formLogin.loginPage("/auth/sign-in");
                    formLogin.permitAll();
                    formLogin.authenticationDetailsSource(twoFactorAuthenticationDetailsSource());
                    formLogin.successHandler(twoFactorAuthenticationHandler());
                    formLogin.failureHandler((request, response, e) -> {
                        log.error(e.getMessage(), e);
                        response.sendRedirect("/auth/sign-in?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
                    });
                })
                .authenticationManager(authenticationManager())
                .logout(logout->logout.logoutUrl("/auth/sign-out"))
                .rememberMe(rememberMe->rememberMe.rememberMeServices(rememberMeService()))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterAt(twoFactorAuthenticationFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public TwoFactorAuthenticationSuccessHandler twoFactorAuthenticationHandler() {
        return new TwoFactorAuthenticationSuccessHandler(rememberMeService());
    }

    @Bean
    public TwoFactorAuthenticationDetailsSource twoFactorAuthenticationDetailsSource() {
        return new TwoFactorAuthenticationDetailsSource();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(new RequestAttributeSecurityContextRepository(), new HttpSessionSecurityContextRepository());
    }

    @Bean
    public TwoFactorAuthenticationRememberMeService rememberMeService() {
        return new TwoFactorAuthenticationRememberMeService(userDetailsService);
    }

    @Bean
    public TwoFactorAuthenticationFilter twoFactorAuthenticationFilter(AuthenticationManager authenticationManager) {
        var filter = new TwoFactorAuthenticationFilter(authenticationManager);
        filter.setAuthenticationDetailsSource(twoFactorAuthenticationDetailsSource());
        filter.setAuthenticationSuccessHandler(twoFactorAuthenticationHandler());
        filter.setSecurityContextRepository(securityContextRepository());
        filter.setRememberMeServices(rememberMeService());
        return filter;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        var usernamePasswordAuthenticationProvider = new TwoFactorUsernamePasswordAuthenticationProvider();
        usernamePasswordAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        usernamePasswordAuthenticationProvider.setUserDetailsService(userDetailsService);
        var twoFactorAuthenticationProvider = new TwoFactorAuthenticationProvider(userDetailsService);
        return new ProviderManager(usernamePasswordAuthenticationProvider, twoFactorAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
