package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.repository.ProductFilter;
import br.com.topone.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ListLabelSuggestionsUseCase {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public ListLabelSuggestionsResult execute(ListLabelSuggestionsCommand command) {
        var targetDate = command.date() != null ? command.date() : LocalDate.now();
        var pageResult = productRepository.findAll(
                new ProductFilter(command.name(), command.sku(), null, command.category(), true, targetDate),
                command.page(),
                command.size(),
                command.sort()
        );

        var content = pageResult.content().stream()
                .map(product -> new ListLabelSuggestionsResult.ProductSuggestion(
                        product.getId(),
                        product.getSku(),
                        product.getBarcode(),
                        product.getName(),
                        product.getCategory(),
                        product.getUnit(),
                        product.getSalePrice(),
                        product.getPromotionalPrice(),
                        product.getCreatedAt()
                ))
                .toList();

        return new ListLabelSuggestionsResult(
                content,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }
}
