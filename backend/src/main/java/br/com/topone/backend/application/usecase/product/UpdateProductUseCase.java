package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.ProductBarcodeAlreadyExistsException;
import br.com.topone.backend.domain.exception.ProductNotFoundException;
import br.com.topone.backend.domain.exception.ProductSkuAlreadyExistsException;
import br.com.topone.backend.domain.exception.SupplierNotFoundException;
import br.com.topone.backend.domain.model.Product;
import br.com.topone.backend.domain.repository.ProductRepository;
import br.com.topone.backend.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;

    @Transactional
    public UpdateProductResult execute(UpdateProductCommand command) {
        var product = productRepository.findById(command.id())
                .orElseThrow(() -> new ProductNotFoundException(command.id()));

        product.changeSku(command.sku());
        product.changeBarcode(command.barcode());
        product.changeName(command.name());
        product.changeDescription(command.description());
        product.changeBrand(command.brand());
        product.changeCategory(command.category());
        product.changeSupplierId(command.supplierId());
        product.changeUnit(command.unit());
        product.changeCostPrice(command.costPrice());
        product.changeSalePrice(command.salePrice());
        product.changePromotionalPrice(command.promotionalPrice());
        product.changeStockQuantity(command.stockQuantity());
        product.changeMinimumStock(command.minimumStock());
        product.changeNcm(command.ncm());
        product.changeCest(command.cest());
        product.changeCfop(command.cfop());
        product.changeTaxOrigin(command.taxOrigin());
        product.changeTaxSituation(command.taxSituation());
        product.changeIcmsRate(command.icmsRate());
        product.changePisSituation(command.pisSituation());
        product.changePisRate(command.pisRate());
        product.changeCofinsSituation(command.cofinsSituation());
        product.changeCofinsRate(command.cofinsRate());
        product.changeImageId(command.imageId());
        validateSupplier(product.getSupplierId());

        productRepository.findBySkuExcludingId(product.getSku(), product.getId())
                .ifPresent(existing -> {
                    throw new ProductSkuAlreadyExistsException(product.getSku());
                });

        if (product.getBarcode() != null) {
            productRepository.findByBarcodeExcludingId(product.getBarcode(), product.getId())
                    .ifPresent(existing -> {
                        throw new ProductBarcodeAlreadyExistsException(product.getBarcode());
                    });
        }

        product.touch();
        var updated = productRepository.save(product);
        log.info("Product updated | sku={} | id={}", updated.getSku(), updated.getId());

        return toResult(updated);
    }

    private UpdateProductResult toResult(Product product) {
        return new UpdateProductResult(
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

    private void validateSupplier(UUID supplierId) {
        if (supplierId == null) {
            return;
        }

        supplierRepository.findById(supplierId)
                .orElseThrow(SupplierNotFoundException::new);
    }
}
