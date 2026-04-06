package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.exception.InvalidTokenException;
import br.com.topone.backend.domain.exception.RefreshTokenExpiredException;
import br.com.topone.backend.domain.exception.RefreshTokenNotFoundException;
import br.com.topone.backend.domain.exception.RefreshTokenRevokedException;
import br.com.topone.backend.domain.model.RefreshToken;
import br.com.topone.backend.domain.repository.RefreshTokenRepository;
import br.com.topone.backend.infrastructure.security.JwtTokenService;
import br.com.topone.backend.infrastructure.security.TokenHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final TokenHashService tokenHashService;

    public RefreshTokenResult execute(RefreshTokenCommand command) {
        var tokenHash = tokenHashService.hash(command.refreshToken());

        var storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(RefreshTokenNotFoundException::new);

        if (storedToken.isRevoked()) {
            storedToken.recordUsage();
            refreshTokenRepository.save(storedToken);
            log.warn("Attempt to use revoked refresh token | tokenId={}", storedToken.getId());
            throw new RefreshTokenRevokedException();
        }

        if (storedToken.isExpired()) {
            storedToken.recordUsage();
            refreshTokenRepository.save(storedToken);
            log.warn("Attempt to use expired refresh token | tokenId={}", storedToken.getId());
            throw new RefreshTokenExpiredException();
        }

        var user = storedToken.getUser();
        var expiresIn = jwtTokenService.getRefreshTokenExpirationSeconds();

        var rawRefreshToken = jwtTokenService.generateRefreshToken(user);
        var newTokenHash = tokenHashService.hash(rawRefreshToken);

        storedToken.recordUsage();
        storedToken.replaceBy(newTokenHash);
        refreshTokenRepository.save(storedToken);

        var newRefreshToken = new RefreshToken(user, newTokenHash, Instant.now().plusSeconds(expiresIn));
        refreshTokenRepository.save(newRefreshToken);

        var accessToken = jwtTokenService.generateAccessToken(user);

        log.info("Token refreshed successfully | userId={}", user.getId());

        return new RefreshTokenResult(accessToken, rawRefreshToken, expiresIn);
    }
}
