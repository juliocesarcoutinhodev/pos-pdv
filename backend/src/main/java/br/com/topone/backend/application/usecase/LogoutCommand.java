package br.com.topone.backend.application.usecase;

public record LogoutCommand(
        String refreshToken,
        boolean revokeAll
) {
}
