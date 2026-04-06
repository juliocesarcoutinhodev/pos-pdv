package br.com.topone.backend.application.usecase;

public record RefreshTokenResult(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
