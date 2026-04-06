package br.com.topone.backend.infrastructure.security;

import br.com.topone.backend.domain.model.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        var authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        var token = authorization.substring(7);
        var claims = tokenService.validateToken(token);

        if (claims == null) {
            filterChain.doFilter(request, response);
            return;
        }

        var user = buildUserFromClaims(claims);
        var authentication = new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private User buildUserFromClaims(Claims claims) {
        var user = new User();
        user.setId(java.util.UUID.fromString(claims.getSubject()));
        user.setEmail(claims.get("email", String.class));
        user.setName(claims.get("name", String.class));
        return user;
    }
}
