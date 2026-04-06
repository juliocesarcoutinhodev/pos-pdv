package br.com.topone.backend.interfaces.dto;

public record AuthResponse(
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
