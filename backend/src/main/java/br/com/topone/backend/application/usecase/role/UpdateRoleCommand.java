package br.com.topone.backend.application.usecase.role;

import java.util.UUID;

public record UpdateRoleCommand(
        UUID id,
        String name,
        String description
) {
}
