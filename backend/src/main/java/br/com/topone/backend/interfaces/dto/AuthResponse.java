package br.com.topone.backend.interfaces.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserResponse user
) {
    public record UserResponse(
            String id,
            String email,
            String name,
            String provider
    ) {
    }
}
