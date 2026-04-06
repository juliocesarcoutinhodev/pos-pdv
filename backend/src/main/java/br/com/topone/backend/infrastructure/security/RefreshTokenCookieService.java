package br.com.topone.backend.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.StringJoiner;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class RefreshTokenCookieService {

    private final RefreshTokenCookieProperties properties;

    public void setCookie(HttpServletResponse response, String token) {
        response.addHeader("Set-Cookie", buildHeader(token, (int) properties.getMaxAge()));
    }

    public void clearCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildHeader("", 0));
    }

    public String getTokenValue(HttpServletRequest request) {
        var cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> properties.getName().equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String buildHeader(String token, int maxAge) {
        var parts = new StringJoiner("; ");
        parts.add(properties.getName() + "=" + token);
        parts.add("Path=" + properties.getPath());
        parts.add("Max-Age=" + maxAge);
        if (properties.isHttpOnly()) {
            parts.add("HttpOnly");
        }
        if (properties.isSecure()) {
            parts.add("Secure");
        }
        if (!properties.getSameSite().isEmpty()) {
            parts.add("SameSite=" + properties.getSameSite());
        }
        if (!properties.getDomain().isBlank()) {
            parts.add("Domain=" + properties.getDomain());
        }
        return parts.toString();
    }
}
