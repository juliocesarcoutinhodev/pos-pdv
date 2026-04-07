package br.com.topone.backend.application.usecase.role;

public record ListRolesCommand(
        int page,
        int size
) {
}
