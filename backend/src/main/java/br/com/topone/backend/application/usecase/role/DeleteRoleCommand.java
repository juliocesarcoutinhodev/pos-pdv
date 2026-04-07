package br.com.topone.backend.application.usecase.role;

import java.util.UUID;

public record DeleteRoleCommand(
        UUID id
) {
}
