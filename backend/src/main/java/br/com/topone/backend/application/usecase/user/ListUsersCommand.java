package br.com.topone.backend.application.usecase.user;

import br.com.topone.backend.domain.repository.UserFilter;
import br.com.topone.backend.domain.repository.PageSort;

public record ListUsersCommand(
        UserFilter filter,
        int page,
        int size,
        PageSort sort
) {
}
