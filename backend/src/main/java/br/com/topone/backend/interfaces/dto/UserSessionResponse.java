package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.domain.model.enums.Role;

import java.util.Set;
import java.util.UUID;

public record UserSessionResponse(
        UUID id,
        String email,
        String name,
        Set<Role> roles
) {
}
