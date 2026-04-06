package br.com.topone.backend.application.usecase.user;

import br.com.topone.backend.domain.repository.UserFilter;

public record ListUsersCommand(
        UserFilter filter,
        int page,
        int size
) {
}
