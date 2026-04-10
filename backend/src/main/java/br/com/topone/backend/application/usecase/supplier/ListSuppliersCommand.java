package br.com.topone.backend.application.usecase.supplier;

import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.SupplierFilter;

public record ListSuppliersCommand(
        SupplierFilter filter,
        int page,
        int size,
        PageSort sort
) {
}
