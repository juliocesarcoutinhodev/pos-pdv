package br.com.topone.backend.application.usecase;

import java.util.UUID;

public record LoginResult(
        UUID id,
        String email,
        String name,
        String provider,
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
}
