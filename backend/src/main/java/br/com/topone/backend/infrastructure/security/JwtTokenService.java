package br.com.topone.backend.infrastructure.security;

import br.com.topone.backend.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProperties properties;

    public String generateAccessToken(User user) {
        var now = Instant.now();
        var expiry = now.plusSeconds(properties.getAccessTokenExpiration());

        var roleNames = user.getRoles() != null
                ? user.getRoles().stream().map(br.com.topone.backend.domain.model.Role::getName).collect(Collectors.toSet())
                : Set.<String>of();

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("roles", roleNames)
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        var now = Instant.now();
        var expiry = now.plusSeconds(properties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .issuer(properties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .requireIssuer(properties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public UUID getUserIdFromToken(String token) {
        var claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        var claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("email", String.class);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(properties.getDecodedSecret());
    }

    public long getRefreshTokenExpirationSeconds() {
        return properties.getRefreshTokenExpiration();
    }
}
