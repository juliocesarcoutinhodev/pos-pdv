package br.com.topone.backend.application.usecase.login;

public record RefreshTokenResult(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
