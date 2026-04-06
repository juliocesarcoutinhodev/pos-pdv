package br.com.topone.backend.application.usecase;

import java.util.Set;
import java.util.UUID;

public record UpdateUserPatchCommand(
        UUID id,
        String email,
        String name,
        String password,
        Set<UUID> roleIds
) {
}
