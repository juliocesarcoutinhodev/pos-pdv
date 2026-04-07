package br.com.topone.backend.application.usecase.role;

import java.util.UUID;

public record UpdateRolePatchCommand(
        UUID id,
        String name,
        String description
) {
}
