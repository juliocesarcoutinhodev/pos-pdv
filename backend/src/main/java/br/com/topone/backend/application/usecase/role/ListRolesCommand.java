package br.com.topone.backend.application.usecase.role;

import br.com.topone.backend.domain.repository.PageSort;

public record ListRolesCommand(
        int page,
        int size,
        PageSort sort
) {
}
