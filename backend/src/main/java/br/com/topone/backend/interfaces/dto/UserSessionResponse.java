package br.com.topone.backend.interfaces.dto;

import java.util.Set;
import java.util.UUID;

public record UserSessionResponse(
        UUID id,
        String email,
        String name,
        Set<String> roles
) {
}
