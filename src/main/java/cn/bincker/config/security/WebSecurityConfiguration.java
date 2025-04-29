package cn.bincker.config.security;

import cn.bincker.common.service.SimpleUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SimpleUserDetailsService userDetailsService) throws Exception {
        return http
                .authorizeHttpRequests(authorize->
                                authorize
                                        .requestMatchers("/todo/**").authenticated()
                                        .anyRequest().permitAll()
                )
                .userDetailsService(userDetailsService)
                .formLogin(Customizer.withDefaults())
                .rememberMe(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}
