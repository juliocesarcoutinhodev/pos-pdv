package br.com.topone.backend.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private static final int CACHE_EXPIRY_MINUTES = 10;

    private final RateLimitProperties properties;

    private final Cache<String, TokenBucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES)
            .maximumSize(100_000)
            .build();

    public RateLimitResult consume(String clientIdentifier, String requestPath) {
        if (!properties.isEnabled()) {
            return RateLimitResult.allowed(-1, -1);
        }

        var config = properties.getConfigForPath(requestPath);
        var cacheKey = clientIdentifier + ":" + requestPath;

        var tokenBucket = buckets.get(cacheKey, key -> {
            var bucket = buildBucket(config);
            return new TokenBucket(bucket, Instant.now());
        });

        tokenBucket.lastAccess = Instant.now();

        var consumed = tokenBucket.bucket.tryConsume(1);
        var available = tokenBucket.bucket.getAvailableTokens();

        if (!consumed) {
            var retryAfter = calculateRetryAfterSeconds();
            log.warn("Rate limit exceeded | ip={} | path={} | retryAfter={}s",
                    clientIdentifier, requestPath, retryAfter);
            return RateLimitResult.rejected(retryAfter, available, config.getCapacity());
        }

        return RateLimitResult.allowed(available, config.getCapacity());
    }

    private Bucket buildBucket(RateLimitProperties.EndpointConfig config) {
        var duration = Duration.ofMinutes(config.getRefillDurationMinutes());
        var limit = Bandwidth.builder()
                .capacity(config.getCapacity())
                .refillGreedy(config.getRefillTokens(), duration)
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private long calculateRetryAfterSeconds() {
        // Use auth endpoint config for refill rate (strictest rate)
        var config = properties.getConfigForPath("/api/v1/auth/");
        var refillDurationMinutes = config.getRefillDurationMinutes();
        var refillTokens = config.getRefillTokens();
        var refillRatePerSecond = (double) refillTokens / (refillDurationMinutes * 60);
        return Math.max(1, (long) Math.ceil(1.0 / refillRatePerSecond));
    }

    private static class TokenBucket {
        final Bucket bucket;
        volatile Instant lastAccess;

        TokenBucket(Bucket bucket, Instant lastAccess) {
            this.bucket = bucket;
            this.lastAccess = lastAccess;
        }
    }

    public record RateLimitResult(
            boolean allowed,
            long retryAfterSeconds,
            long remainingTokens,
            long limit
    ) {
        public static RateLimitResult allowed(long remaining, long limit) {
            return new RateLimitResult(true, 0, remaining, limit);
        }

        public static RateLimitResult rejected(long retryAfter, long remaining, long limit) {
            return new RateLimitResult(false, retryAfter, remaining, limit);
        }
    }
}
