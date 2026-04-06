package br.com.topone.backend.interfaces.dto;

public record RefreshResponse(
        String accessToken,
        Long expiresIn
) {
}
