package br.com.topone.backend.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    private String[] allowedOrigins = new String[]{"http://localhost:3000", "http://localhost:5173"};
    private String[] allowedMethods = new String[]{"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};
    private String[] allowedHeaders = new String[]{"Authorization", "Content-Type", "X-Correlation-Id"};
    private String[] exposedHeaders = new String[]{
            "X-Correlation-Id",
            "X-RateLimit-Limit",
            "X-RateLimit-Remaining",
            "X-RateLimit-Retry-After-Seconds",
            "Retry-After"
    };
    private boolean allowCredentials = true;
    private long maxAge = 3600;
}
