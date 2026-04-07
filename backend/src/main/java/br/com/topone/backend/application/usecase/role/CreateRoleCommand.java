package br.com.topone.backend.application.usecase.role;

public record CreateRoleCommand(
        String name,
        String description
) {
}
