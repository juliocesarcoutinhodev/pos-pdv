package br.com.topone.backend.interfaces.dto;

public record LoginResponse(
        LoginResponse.UserResponse user,
        String accessToken,
        Long expiresIn
) {
    public record UserResponse(
            String id,
            String email,
            String name,
            String provider
    ) {
    }
}
