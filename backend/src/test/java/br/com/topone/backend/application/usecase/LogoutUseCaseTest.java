package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.exception.InvalidTokenException;
import br.com.topone.backend.domain.model.RefreshToken;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.AuthProvider;
import br.com.topone.backend.domain.repository.RefreshTokenRepository;
import br.com.topone.backend.infrastructure.security.TokenHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private TokenHashService tokenHashService;

    @InjectMocks
    private LogoutUseCase useCase;

    private User user;
    private RefreshToken activeToken;
    private String rawToken;
    private String hashedToken;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setPasswordHash("hashed");
        user.setProvider(AuthProvider.LOCAL);

        rawToken = "raw-refresh-token";
        hashedToken = "hashed-refresh-token";

        activeToken = new RefreshToken();
        activeToken.setId(UUID.randomUUID());
        activeToken.setUser(user);
        activeToken.setTokenHash(hashedToken);
        activeToken.setExpiresAt(Instant.now().plusSeconds(604800));
        activeToken.setCreatedAt(Instant.now());
    }

    @Test
    void shouldRevokeSingleToken() {
        var command = new LogoutCommand(rawToken, false);

        when(tokenHashService.hash(rawToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(activeToken));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        useCase.execute(command);

        assertThat(activeToken.getRevokedAt()).isNotNull();
        verify(refreshTokenRepository).save(activeToken);
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    void shouldRevokeAllTokensWhenRevokeAllIsTrue() {
        var command = new LogoutCommand(rawToken, true);

        when(tokenHashService.hash(rawToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(activeToken));

        useCase.execute(command);

        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenTokenNotFound() {
        var command = new LogoutCommand(rawToken, false);

        when(tokenHashService.hash(rawToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    void shouldNotSaveIfTokenAlreadyRevoked() {
        activeToken.setRevokedAt(Instant.now());

        var command = new LogoutCommand(rawToken, false);

        when(tokenHashService.hash(rawToken)).thenReturn(hashedToken);
        when(refreshTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(activeToken));

        useCase.execute(command);

        verify(refreshTokenRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }
}
