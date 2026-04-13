package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.ProductBarcodeAlreadyExistsException;
import br.com.topone.backend.domain.exception.ProductNotFoundException;
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
public class UpdateProductPatchUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public UpdateProductResult execute(UpdateProductPatchCommand command) {
        var product = productRepository.findById(command.id())
                .orElseThrow(() -> new ProductNotFoundException(command.id()));

        if (command.sku() != null) {
            product.changeSku(command.sku());
        }
        if (command.barcode() != null) {
            product.changeBarcode(command.barcode());
        }
        if (command.name() != null) {
            product.changeName(command.name());
        }
        if (command.description() != null) {
            product.changeDescription(command.description());
        }
        if (command.brand() != null) {
            product.changeBrand(command.brand());
        }
        if (command.category() != null) {
            product.changeCategory(command.category());
        }
        if (command.unit() != null) {
            product.changeUnit(command.unit());
        }
        if (command.costPrice() != null) {
            product.changeCostPrice(command.costPrice());
        }
        if (command.salePrice() != null) {
            product.changeSalePrice(command.salePrice());
        }
        if (command.promotionalPrice() != null) {
            product.changePromotionalPrice(command.promotionalPrice());
        }
        if (command.stockQuantity() != null) {
            product.changeStockQuantity(command.stockQuantity());
        }
        if (command.minimumStock() != null) {
            product.changeMinimumStock(command.minimumStock());
        }
        if (command.ncm() != null) {
            product.changeNcm(command.ncm());
        }
        if (command.cest() != null) {
            product.changeCest(command.cest());
        }
        if (command.cfop() != null) {
            product.changeCfop(command.cfop());
        }
        if (command.taxOrigin() != null) {
            product.changeTaxOrigin(command.taxOrigin());
        }
        if (command.taxSituation() != null) {
            product.changeTaxSituation(command.taxSituation());
        }
        if (command.icmsRate() != null) {
            product.changeIcmsRate(command.icmsRate());
        }
        if (command.pisSituation() != null) {
            product.changePisSituation(command.pisSituation());
        }
        if (command.pisRate() != null) {
            product.changePisRate(command.pisRate());
        }
        if (command.cofinsSituation() != null) {
            product.changeCofinsSituation(command.cofinsSituation());
        }
        if (command.cofinsRate() != null) {
            product.changeCofinsRate(command.cofinsRate());
        }
        if (command.imageId() != null) {
            product.changeImageId(command.imageId());
        }
        if (command.active() != null) {
            if (command.active()) {
                product.reactivate();
            } else {
                product.deactivate();
            }
        }

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
        log.info("Product partially updated | sku={} | id={}", updated.getSku(), updated.getId());

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
}
