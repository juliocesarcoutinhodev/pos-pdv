package br.com.topone.backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    private static final String[] SKIP_PATTERNS = {
            "/actuator/",
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        var path = request.getRequestURI().substring(request.getContextPath().length());

        if (shouldSkipRateLimit(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        var clientIp = resolveClientIp(request);
        var result = rateLimiterService.consume(clientIp, path);

        if (!result.allowed()) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));
            response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));
            response.setHeader("X-RateLimit-Retry-After-Seconds", String.valueOf(result.retryAfterSeconds()));
            response.getWriter().write(String.format(
                    "{\"error\":\"Muitas requisicoes\",\"message\":\"Tente novamente em %d segundos\"}",
                    result.retryAfterSeconds()
            ));
            return;
        }

        response.setHeader("X-RateLimit-Limit", String.valueOf(result.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipRateLimit(String path) {
        for (var pattern : SKIP_PATTERNS) {
            if (path.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String resolveClientIp(HttpServletRequest request) {
        var forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        var realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
