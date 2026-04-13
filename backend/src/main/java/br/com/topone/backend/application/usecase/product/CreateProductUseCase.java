package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.ProductBarcodeAlreadyExistsException;
import br.com.topone.backend.domain.exception.ProductSkuAlreadyExistsException;
import br.com.topone.backend.domain.model.Product;
import br.com.topone.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public CreateProductResult execute(CreateProductCommand command) {
        var product = Product.create(
                command.sku(),
                command.barcode(),
                command.name(),
                command.description(),
                command.brand(),
                command.category(),
                command.unit(),
                command.costPrice(),
                command.salePrice(),
                command.promotionalPrice(),
                command.stockQuantity(),
                command.minimumStock(),
                command.ncm(),
                command.cest(),
                command.cfop(),
                command.taxOrigin(),
                command.taxSituation(),
                command.icmsRate(),
                command.pisSituation(),
                command.pisRate(),
                command.cofinsSituation(),
                command.cofinsRate(),
                command.imageId()
        );

        if (productRepository.existsBySku(product.getSku())) {
            throw new ProductSkuAlreadyExistsException(product.getSku());
        }

        if (product.getBarcode() != null && productRepository.existsByBarcode(product.getBarcode())) {
            throw new ProductBarcodeAlreadyExistsException(product.getBarcode());
        }

        var saved = productRepository.save(product);
        log.info("Product created | sku={} | id={}", saved.getSku(), saved.getId());

        return new CreateProductResult(
                saved.getId(),
                saved.getSku(),
                saved.getBarcode(),
                saved.getName(),
                saved.getDescription(),
                saved.getBrand(),
                saved.getCategory(),
                saved.getUnit(),
                saved.getCostPrice(),
                saved.getSalePrice(),
                saved.getPromotionalPrice(),
                saved.getStockQuantity(),
                saved.getMinimumStock(),
                saved.getNcm(),
                saved.getCest(),
                saved.getCfop(),
                saved.getTaxOrigin(),
                saved.getTaxSituation(),
                saved.getIcmsRate(),
                saved.getPisSituation(),
                saved.getPisRate(),
                saved.getCofinsSituation(),
                saved.getCofinsRate(),
                saved.getImageId(),
                saved.getCreatedAt()
        );
    }
}
