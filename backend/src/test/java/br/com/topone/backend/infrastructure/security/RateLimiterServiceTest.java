package br.com.topone.backend.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;
    private RateLimitProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.getCache().setExpiryMinutes(10);
        properties.getCache().setMaximumSize(100_000);
        properties.getAuth().setCapacity(3);
        properties.getAuth().setRefillTokens(3);
        properties.getAuth().setRefillDurationMinutes(1);
        properties.getDefaultConfig().setCapacity(10);
        properties.getDefaultConfig().setRefillTokens(10);
        properties.getDefaultConfig().setRefillDurationMinutes(1);
        rateLimiterService = new RateLimiterService(properties);
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        var r1 = rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");
        assertThat(r1.allowed()).isTrue();
        assertThat(r1.remainingTokens()).isEqualTo(2);

        var r2 = rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");
        assertThat(r2.allowed()).isTrue();
        assertThat(r2.remainingTokens()).isEqualTo(1);

        var r3 = rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");
        assertThat(r3.allowed()).isTrue();
        assertThat(r3.remainingTokens()).isEqualTo(0);
    }

    @Test
    void shouldRejectRequestsAfterLimitExceeded() {
        rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");
        rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");
        rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");

        var r4 = rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");
        assertThat(r4.allowed()).isFalse();
        assertThat(r4.retryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    void shouldTrackDifferentIpsIndependently() {
        rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");
        var resultA = rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");

        var resultB = rateLimiterService.consume("10.0.0.1", "/api/v1/auth/login");
        assertThat(resultB.remainingTokens()).isEqualTo(2);
    }

    @Test
    void shouldUseDefaultBucketForNonAuthEndpoints() {
        var r1 = rateLimiterService.consume("127.0.0.1", "/api/v1/me");
        assertThat(r1.allowed()).isTrue();
        assertThat(r1.remainingTokens()).isEqualTo(9);
    }

    @Test
    void shouldReturnAllowedWhenDisabled() {
        properties.setEnabled(false);
        var result = rateLimiterService.consume("127.0.0.1", "/api/v1/auth/login");
        assertThat(result.allowed()).isTrue();
        assertThat(result.remainingTokens()).isEqualTo(-1);
    }
}
