package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.ProductFilter;

public record ListProductsCommand(
        ProductFilter filter,
        int page,
        int size,
        PageSort sort
) {
}
