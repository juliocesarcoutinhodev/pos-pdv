package br.com.topone.backend.interfaces.dto;

public record LoginResponse(
        UserResponse user,
        String accessToken,
        String refreshToken,
        Long expiresIn
) {
    public record UserResponse(
            String id,
            String email,
            String name,
            String provider
    ) {}
}
