package it.unical.immobiliare.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SessionSecurityWebConfig implements WebMvcConfigurer {
    private final AccountSessionInterceptor accounts;

    public SessionSecurityWebConfig(AccountSessionInterceptor accounts) {
        this.accounts = accounts;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accounts)
                .addPathPatterns("/api/**")
                // Permetti sempre un nuovo login e il logout anche a sessione obsoleta.
                .excludePathPatterns("/api/auth/login", "/api/auth/logout",
                        "/api/auth/csrf", "/api/auth/registrazione");
    }
}
