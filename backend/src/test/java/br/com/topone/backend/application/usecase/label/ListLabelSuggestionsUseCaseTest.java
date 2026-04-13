package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.model.Product;
import br.com.topone.backend.domain.repository.PageResult;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.ProductFilter;
import br.com.topone.backend.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListLabelSuggestionsUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ListLabelSuggestionsUseCase useCase;

    @Test
    void shouldListSuggestionsUsingCurrentDateWhenDateIsNotInformed() {
        var product = Product.create(
                "SKU-001",
                "7890001112223",
                "Produto A",
                null,
                null,
                "Categoria",
                null,
                "UN",
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
                new BigDecimal("13.00"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        product.setId(UUID.randomUUID());

        when(productRepository.findAll(org.mockito.ArgumentMatchers.any(ProductFilter.class), anyInt(), anyInt(), eq(PageSort.unsorted())))
                .thenReturn(new PageResult<>(List.of(product), 0, 20, 1, 1));

        var command = new ListLabelSuggestionsCommand(
                null,
                "Produto",
                "SKU",
                "Categoria",
                0,
                20,
                PageSort.unsorted()
        );

        var result = useCase.execute(command);

        var filterCaptor = ArgumentCaptor.forClass(ProductFilter.class);
        org.mockito.Mockito.verify(productRepository).findAll(filterCaptor.capture(), eq(0), eq(20), eq(PageSort.unsorted()));
        assertThat(filterCaptor.getValue().active()).isTrue();
        assertThat(filterCaptor.getValue().createdDate()).isEqualTo(LocalDate.now());

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().sku()).isEqualTo("SKU-001");
        assertThat(result.content().getFirst().salePrice()).isEqualByComparingTo("15.00");
        assertThat(result.content().getFirst().promotionalPrice()).isEqualByComparingTo("13.00");
    }
}
