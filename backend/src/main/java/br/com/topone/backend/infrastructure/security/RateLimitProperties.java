package br.com.topone.backend.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    private boolean enabled = true;

    private Cache cache = new Cache(10, 100_000);
    private EndpointConfig auth = new EndpointConfig();
    private EndpointConfig defaultConfig = new EndpointConfig();

    public EndpointConfig getConfigForPath(String path) {
        if (path.startsWith("/api/v1/auth/")) {
            return auth;
        }
        return defaultConfig;
    }

    @Getter
    @Setter
    public static class Cache {
        private long expiryMinutes;
        private long maximumSize;

        public Cache() {
        }

        public Cache(long expiryMinutes, long maximumSize) {
            this.expiryMinutes = expiryMinutes;
            this.maximumSize = maximumSize;
        }
    }

    @Getter
    @Setter
    public static class EndpointConfig {
        private long capacity;
        private long refillTokens;
        private long refillDurationMinutes;

        public EndpointConfig() {
        }

        public EndpointConfig(long capacity, long refillTokens, long refillDurationMinutes) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillDurationMinutes = refillDurationMinutes;
        }
    }
}
