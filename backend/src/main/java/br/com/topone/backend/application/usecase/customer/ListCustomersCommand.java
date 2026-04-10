package br.com.topone.backend.application.usecase.customer;

import br.com.topone.backend.domain.repository.CustomerFilter;
import br.com.topone.backend.domain.repository.PageSort;

public record ListCustomersCommand(
        CustomerFilter filter,
        int page,
        int size,
        PageSort sort
) {
}
