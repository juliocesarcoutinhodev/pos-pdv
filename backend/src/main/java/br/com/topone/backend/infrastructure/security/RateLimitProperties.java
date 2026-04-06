package br.com.topone.backend.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private EndpointConfig auth = new EndpointConfig(5, 5, 1);
    private EndpointConfig defaultConfig = new EndpointConfig(100, 100, 1);

    @PostConstruct
    public void init() {
        // ensure non-auth endpoints also get defaults if not explicitly configured
    }

    public EndpointConfig getConfigForPath(String path) {
        if (path.startsWith("/api/v1/auth/")) {
            return auth;
        }
        return defaultConfig;
    }

    @Getter
    @Setter
    public static class EndpointConfig {
        private long capacity;
        private long refillTokens;
        private long refillDurationMinutes;

        public EndpointConfig() {
            // for Spring binding
        }

        public EndpointConfig(long capacity, long refillTokens, long refillDurationMinutes) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillDurationMinutes = refillDurationMinutes;
        }
    }
}
