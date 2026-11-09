package it.unical.immobiliare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

            http
                .csrf(csrf -> csrf.spa())
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/api/annunci", "/api/annunci/**")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf")
                    .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/registrazione", "/api/auth/login")
                    .permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}