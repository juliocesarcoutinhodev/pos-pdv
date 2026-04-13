package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GetNextProductSkuUseCase {

    private static final int MIN_SKU_VALUE = 100000;
    private static final int MAX_SKU_VALUE_EXCLUSIVE = 1_000_000;
    private static final int MAX_GENERATION_ATTEMPTS = 30;

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public String execute() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            var suggestedSku = Integer.toString(
                    ThreadLocalRandom.current().nextInt(MIN_SKU_VALUE, MAX_SKU_VALUE_EXCLUSIVE)
            );
            if (!productRepository.existsBySku(suggestedSku)) {
                return suggestedSku;
            }
        }

        throw new IllegalStateException("Unable to generate unique random SKU suggestion.");
    }
}
