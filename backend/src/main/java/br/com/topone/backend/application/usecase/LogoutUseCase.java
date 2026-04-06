package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.exception.InvalidTokenException;
import br.com.topone.backend.domain.repository.RefreshTokenRepository;
import br.com.topone.backend.infrastructure.security.TokenHashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashService tokenHashService;

    @Transactional
    public void execute(LogoutCommand command) {
        var tokenHash = tokenHashService.hash(command.refreshToken());

        var storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidTokenException::new);

        if (command.revokeAll()) {
            var userId = storedToken.getUser().getId();
            refreshTokenRepository.revokeAllByUserId(userId);
            log.info("All tokens revoked | userId={}", userId);
            return;
        }

        if (!storedToken.isRevoked()) {
            storedToken.revoke();
            refreshTokenRepository.save(storedToken);
            log.info("Token revoked | tokenId={}", storedToken.getId());
        }
    }
}
