package br.com.topone.backend.application.usecase;

import java.util.UUID;

public record RegisterUserResult(
        UUID id,
        String email,
        String name,
        String provider
) {
}
