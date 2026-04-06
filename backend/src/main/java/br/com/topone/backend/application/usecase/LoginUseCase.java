package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.exception.InvalidCredentialsException;
import br.com.topone.backend.domain.model.RefreshToken;
import br.com.topone.backend.domain.repository.RefreshTokenRepository;
import br.com.topone.backend.domain.repository.UserRepository;
import br.com.topone.backend.infrastructure.security.JwtTokenService;
import br.com.topone.backend.infrastructure.security.TokenHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final TokenHashService tokenHashService;

    public LoginResult execute(LoginCommand command) {
        var user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        var accessToken = jwtTokenService.generateAccessToken(user);
        var rawRefreshToken = jwtTokenService.generateRefreshToken(user);
        var tokenHash = tokenHashService.hash(rawRefreshToken);

        var expiresAt = Instant.now().plusSeconds(jwtTokenService.getRefreshTokenExpirationSeconds());
        var refreshToken = new RefreshToken(user, tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);

        log.info("User logged in successfully | email={} | id={}", user.getEmail(), user.getId());

        return new LoginResult(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProvider().name(),
                accessToken,
                rawRefreshToken,
                jwtTokenService.getRefreshTokenExpirationSeconds()
        );
    }
}
