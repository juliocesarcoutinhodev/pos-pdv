package br.com.topone.backend.interfaces.dto;

import java.util.UUID;

public record ContactResponse(
        UUID id,
        String name,
        String email,
        String phone
) {
}
