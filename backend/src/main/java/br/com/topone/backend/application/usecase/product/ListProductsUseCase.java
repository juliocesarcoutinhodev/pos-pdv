package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListProductsUseCase {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ListProductsResult execute(ListProductsCommand command) {
        var page = productRepository.findAll(command.filter(), command.page(), command.size(), command.sort());
        var content = page.content().stream()
                .map(product -> new ListProductsResult.ProductSummary(
                        product.getId(),
                        product.getSku(),
                        product.getBarcode(),
                        product.getName(),
                        product.getCategory(),
                        product.getSupplierId(),
                        product.getBrand(),
                        product.getUnit(),
                        product.getSalePrice(),
                        product.getStockQuantity(),
                        product.getMinimumStock(),
                        product.getImageId(),
                        product.getCreatedAt(),
                        product.getUpdatedAt(),
                        product.isActive()
                ))
                .toList();

        return new ListProductsResult(
                content,
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
