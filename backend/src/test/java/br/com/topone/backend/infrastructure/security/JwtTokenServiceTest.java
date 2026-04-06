package br.com.topone.backend.infrastructure.security;

import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.AuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private JwtTokenService tokenService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("cG9zcGR2LXNlY3JldC1rZXktZm9yLXRlc3RpbmctcHVycG9zZXMtc2VjdXJl"); // 56 bytes base64
        jwtProperties.setIssuer("pospdv");
        jwtProperties.setAccessTokenExpiration(3600);
        jwtProperties.init();

        tokenService = new JwtTokenService(jwtProperties);
    }

    @Test
    void shouldGenerateValidAccessToken() {
        var user = new User(UUID.randomUUID(), "user@test.com", "Test User", "hash123", AuthProvider.LOCAL, Instant.now(), Instant.now());

        var token = tokenService.generateAccessToken(user);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void shouldValidateTokenWithCorrectIssuer() {
        var user = new User(UUID.randomUUID(), "user@test.com", "Test User", null, AuthProvider.LOCAL, Instant.now(), Instant.now());
        var token = tokenService.generateAccessToken(user);

        var claims = tokenService.validateToken(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(user.getId().toString());
        assertThat(claims.getIssuer()).isEqualTo("pospdv");
        assertThat(claims.get("email", String.class)).isEqualTo("user@test.com");
        assertThat(claims.get("name", String.class)).isEqualTo("Test User");
    }

    @Test
    void shouldRejectTokenWithWrongIssuer() {
        var wrongProps = new JwtProperties();
        wrongProps.setSecret(jwtProperties.getSecret());
        wrongProps.setIssuer("other-issuer");
        wrongProps.setAccessTokenExpiration(3600);
        wrongProps.init();

        var user = new User(UUID.randomUUID(), "user@test.com", "Test User", null, AuthProvider.LOCAL, Instant.now(), Instant.now());
        var token = new JwtTokenService(wrongProps).generateAccessToken(user);

        var claims = tokenService.validateToken(token);

        assertThat(claims).isNull();
    }

    @Test
    void shouldRejectTokenWithWrongSecret() {
        var wrongProps = new JwtProperties();
        wrongProps.setSecret("d3Jvbmctc2VjcmV0LWZvci10ZXN0aW5nLXB1cnBvc2VzLXNlY3VyZQ==");
        wrongProps.setIssuer("pospdv");
        wrongProps.setAccessTokenExpiration(3600);
        wrongProps.init();

        var user = new User(UUID.randomUUID(), "user@test.com", "Test User", null, AuthProvider.LOCAL, Instant.now(), Instant.now());
        var token = new JwtTokenService(wrongProps).generateAccessToken(user);

        var claims = tokenService.validateToken(token);

        assertThat(claims).isNull();
    }

    @Test
    void shouldRejectExpiredToken() {
        jwtProperties.setAccessTokenExpiration(-1);

        var user = new User(UUID.randomUUID(), "user@test.com", "Test User", null, AuthProvider.LOCAL, Instant.now(), Instant.now());
        var token = tokenService.generateAccessToken(user);

        var claims = tokenService.validateToken(token);

        assertThat(claims).isNull();
    }

    @Test
    void shouldExtractUserIdFromValidToken() {
        var userId = UUID.randomUUID();
        var user = new User(userId, "user@test.com", "Test User", null, AuthProvider.LOCAL, Instant.now(), Instant.now());
        var token = tokenService.generateAccessToken(user);

        var extractedId = tokenService.getUserIdFromToken(token);

        assertThat(extractedId).isEqualTo(userId);
    }

    @Test
    void shouldExtractEmailFromValidToken() {
        var user = new User(UUID.randomUUID(), "user@test.com", "Test User", null, AuthProvider.LOCAL, Instant.now(), Instant.now());
        var token = tokenService.generateAccessToken(user);

        var email = tokenService.getEmailFromToken(token);

        assertThat(email).isEqualTo("user@test.com");
    }

    @Test
    void shouldReturnNullForMalformedToken() {
        var claims = tokenService.validateToken("invalid-token-format");

        assertThat(claims).isNull();
    }

    @Test
    void shouldReturnNullForEmptyToken() {
        var claims = tokenService.validateToken("");

        assertThat(claims).isNull();
    }

    @Test
    void shouldReturnNullForGetUserIdWithInvalidToken() {
        var userId = tokenService.getUserIdFromToken("invalid-token");

        assertThat(userId).isNull();
    }
}
