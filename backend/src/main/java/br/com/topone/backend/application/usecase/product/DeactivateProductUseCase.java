package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.ProductNotFoundException;
import br.com.topone.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeactivateProductUseCase {

    private final ProductRepository productRepository;

    @Transactional
    public DeactivateProductResult execute(DeactivateProductCommand command) {
        var product = productRepository.findById(command.id())
                .orElseThrow(() -> new ProductNotFoundException(command.id()));

        product.deactivate();
        product.touch();
        var updated = productRepository.save(product);
        log.info("Product deactivated | sku={} | id={}", updated.getSku(), updated.getId());

        return new DeactivateProductResult(updated.getId(), updated.isActive());
    }
}
