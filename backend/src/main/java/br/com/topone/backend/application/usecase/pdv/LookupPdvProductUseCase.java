package br.com.topone.backend.application.usecase.pdv;

import br.com.topone.backend.domain.exception.ProductNotFoundException;
import br.com.topone.backend.domain.model.Product;
import br.com.topone.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LookupPdvProductUseCase {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public PdvProductLookupResult execute(PdvProductLookupCommand command) {
        var input = command.code() == null ? "" : command.code().trim();
        if (input.isEmpty()) {
            throw new ProductNotFoundException();
        }

        var normalizedBarcode = Product.normalizeBarcode(input);
        var normalizedSku = Product.normalizeSku(input);

        var product = (normalizedBarcode != null ? productRepository.findActiveByBarcode(normalizedBarcode) : java.util.Optional.<Product>empty())
                .or(() -> normalizedSku != null ? productRepository.findActiveBySku(normalizedSku) : java.util.Optional.empty())
                .orElseThrow(ProductNotFoundException::new);

        var unitPrice = product.getPromotionalPrice() != null && product.getPromotionalPrice().compareTo(java.math.BigDecimal.ZERO) > 0
                ? product.getPromotionalPrice()
                : product.getSalePrice();

        return new PdvProductLookupResult(
                product.getId(),
                product.getSku(),
                product.getBarcode(),
                product.getName(),
                product.getUnit(),
                unitPrice,
                product.getStockQuantity()
        );
    }
}
