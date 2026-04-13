package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.ProductNotFoundException;
import br.com.topone.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProductByIdUseCase {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public GetProductByIdResult execute(GetProductByIdCommand command) {
        var product = productRepository.findById(command.id())
                .orElseThrow(() -> new ProductNotFoundException(command.id()));

        return new GetProductByIdResult(
                product.getId(),
                product.getSku(),
                product.getBarcode(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getCategory(),
                product.getSupplierId(),
                product.getUnit(),
                product.getCostPrice(),
                product.getSalePrice(),
                ProductPricingCalculator.calculateMarginPercentage(product.getCostPrice(), product.getSalePrice()),
                product.getPromotionalPrice(),
                product.getStockQuantity(),
                product.getMinimumStock(),
                product.getNcm(),
                product.getCest(),
                product.getCfop(),
                product.getTaxOrigin(),
                product.getTaxSituation(),
                product.getIcmsRate(),
                product.getPisSituation(),
                product.getPisRate(),
                product.getCofinsSituation(),
                product.getCofinsRate(),
                product.getImageId(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.isActive()
        );
    }
}
