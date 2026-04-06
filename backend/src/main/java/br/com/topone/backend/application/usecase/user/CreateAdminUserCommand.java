package br.com.topone.backend.application.usecase.user;

import java.util.Set;
import java.util.UUID;

public record CreateAdminUserCommand(
        String email,
        String name,
        String password,
        Set<UUID> roleIds
) {
}
