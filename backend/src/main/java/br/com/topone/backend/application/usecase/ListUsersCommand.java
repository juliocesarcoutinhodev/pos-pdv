package br.com.topone.backend.application.usecase;

import br.com.topone.backend.domain.repository.UserFilter;

public record ListUsersCommand(
        UserFilter filter,
        int page,
        int size
) {
}
