package com.community.site.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable()))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers("/h2-console/**").permitAll();
                auth.requestMatchers("/admin/**").hasRole("ADMIN");
                auth.requestMatchers("/posts/**", "/profile/**").authenticated();
                auth.anyRequest().permitAll();
            })
            .formLogin(form -> {
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/")
                    .failureUrl("/login?error=true")
                    .permitAll();
            })
            .logout(logout -> {
                logout.logoutUrl("/logout")
                      .logoutSuccessUrl("/")
                      .invalidateHttpSession(true)
                      .clearAuthentication(true)
                      .deleteCookies("JSESSIONID", "remember-me")
                      .permitAll();
            });

        return http.build();
    }
} 