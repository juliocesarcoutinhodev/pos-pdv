package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.exception.InvalidCredentialsException;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.AuthProvider;
import br.com.topone.backend.domain.repository.RefreshTokenRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import br.com.topone.backend.infrastructure.security.JwtTokenService;
import br.com.topone.backend.infrastructure.security.TokenHashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private TokenHashService tokenHashService;

    @InjectMocks
    private LoginUseCase useCase;

    private User user;
    private LoginCommand command;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@test.com");
        user.setName("Test User");
        user.setPasswordHash("hashedPassword");
        user.setProvider(AuthProvider.LOCAL);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        command = new LoginCommand("user@test.com", "password123");
    }

    @Test
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail(command.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(command.password(), user.getPasswordHash())).thenReturn(true);
        when(jwtTokenService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtTokenService.generateRefreshToken(user)).thenReturn("refreshToken");
        when(jwtTokenService.getRefreshTokenExpirationSeconds()).thenReturn(604800L);
        when(tokenHashService.hash("refreshToken")).thenReturn("hashedRefreshToken");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        var result = useCase.execute(command);

        assertThat(result.id()).isEqualTo(user.getId());
        assertThat(result.email()).isEqualTo("user@test.com");
        assertThat(result.name()).isEqualTo("Test User");
        assertThat(result.provider()).isEqualTo("LOCAL");
        assertThat(result.accessToken()).isEqualTo("accessToken");
        assertThat(result.refreshToken()).isEqualTo("refreshToken");
        assertThat(result.expiresIn()).isEqualTo(604800L);

        verify(refreshTokenRepository, times(1)).save(any());
    }

    @Test
    void shouldThrowWhenEmailNotFound() {
        when(userRepository.findByEmail(command.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtTokenService, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPasswordIsWrong() {
        when(userRepository.findByEmail(command.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(command.password(), user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(jwtTokenService, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }
}
