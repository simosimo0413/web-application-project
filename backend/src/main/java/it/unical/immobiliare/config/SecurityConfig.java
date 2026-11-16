package it.unical.immobiliare.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
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
                // Consenti i dispatch di errore, senza cambiare i permessi delle API.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/annunci", "/api/annunci/**")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf")
                    .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/registrazione", "/api/auth/login")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categorie")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/foto/file/**")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/recensioni/annunci/**")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/aste/annunci/**")
                    .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/offerte/aste/*/riepilogo")
                    .permitAll()
                .anyRequest().authenticated()


            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) ->
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT)
                )
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}