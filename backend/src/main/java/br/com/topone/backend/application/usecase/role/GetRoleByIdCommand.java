package br.com.topone.backend.application.usecase.role;

import java.util.UUID;

public record GetRoleByIdCommand(
        UUID id
) {
}
