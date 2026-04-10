package br.com.topone.backend.infrastructure.external.cnpja;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cache settings for CNPJ/ZIP provider lookups.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.lookup-cache")
public class LookupCacheProperties {

    private boolean enabled = true;
    private CacheConfig cnpj = new CacheConfig(1440, 60, 50_000);
    private CacheConfig zip = new CacheConfig(43200, 60, 50_000);

    @Getter
    @Setter
    public static class CacheConfig {
        private long successTtlMinutes;
        private long notFoundTtlMinutes;
        private long maximumSize;

        public CacheConfig() {
        }

        public CacheConfig(long successTtlMinutes, long notFoundTtlMinutes, long maximumSize) {
            this.successTtlMinutes = successTtlMinutes;
            this.notFoundTtlMinutes = notFoundTtlMinutes;
            this.maximumSize = maximumSize;
        }
    }
}
