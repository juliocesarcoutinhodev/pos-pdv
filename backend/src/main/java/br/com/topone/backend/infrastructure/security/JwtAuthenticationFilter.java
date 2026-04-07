package br.com.topone.backend.infrastructure.security;

import br.com.topone.backend.domain.model.User;
import io.jsonwebtoken.Claims;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        var token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            var claims = tokenService.validateToken(token);
            if (claims == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"status\":401,\"error\":\"Não autorizado\",\"message\":\"Token inválido ou expirado\"}");
                return;
            }

            var user = buildUserFromClaims(claims);
            var authentication = new UsernamePasswordAuthenticationToken(user, null, buildAuthorities(user));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.warn("JWT validation error | ip={} path={}", getClientIp(request), request.getRequestURI(), e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"error\":\"Não autorizado\",\"message\":\"Erro ao validar token de acesso\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private User buildUserFromClaims(Claims claims) {
        var user = new User();
        user.setId(java.util.UUID.fromString(claims.getSubject()));
        user.setEmail(claims.get("email", String.class));
        user.setName(claims.get("name", String.class));

        @SuppressWarnings("unchecked")
        var roleList = (List<String>) claims.get("roles", List.class);
        if (roleList != null && !roleList.isEmpty()) {
            var roles = roleList.stream()
                    .map(name -> br.com.topone.backend.domain.model.Role.builder().name(name).build())
                    .collect(Collectors.toCollection(HashSet::new));
            user.setRoles(roles);
        }

        return user;
    }

    private List<SimpleGrantedAuthority> buildAuthorities(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return List.of();
        }
        return user.getRoles().stream()
                .map(r -> "ROLE_" + r.getName())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    private String getClientIp(HttpServletRequest request) {
        var forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
