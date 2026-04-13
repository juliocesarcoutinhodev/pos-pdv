package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.exception.InvalidLabelPrintJobException;
import br.com.topone.backend.domain.exception.ProductNotFoundException;
import br.com.topone.backend.domain.model.LabelPrintJob;
import br.com.topone.backend.domain.model.Product;
import br.com.topone.backend.domain.repository.LabelPrintJobRepository;
import br.com.topone.backend.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateLabelPrintJobUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private LabelPrintJobRepository labelPrintJobRepository;

    @InjectMocks
    private CreateLabelPrintJobUseCase useCase;

    @Test
    void shouldCreateLabelPrintJobAndGroupDuplicatedProducts() {
        var productId = UUID.randomUUID();
        var command = new CreateLabelPrintJobCommand(
                LocalDate.now(),
                List.of(
                        new CreateLabelPrintJobItemCommand(productId, 2),
                        new CreateLabelPrintJobItemCommand(productId, 3)
                )
        );

        var product = createMinimalProduct(productId, "SKU-001", "Produto A");
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(labelPrintJobRepository.save(any(LabelPrintJob.class))).thenAnswer(invocation -> {
            var job = invocation.getArgument(0, LabelPrintJob.class);
            job.setId(UUID.randomUUID());
            job.setCreatedAt(Instant.now());
            return job;
        });

        var result = useCase.execute(command);

        assertThat(result.id()).isNotNull();
        assertThat(result.totalProducts()).isEqualTo(1);
        assertThat(result.totalLabels()).isEqualTo(5);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().productId()).isEqualTo(productId);
        assertThat(result.items().getFirst().quantity()).isEqualTo(5);
        verify(labelPrintJobRepository).save(any(LabelPrintJob.class));
    }

    @Test
    void shouldThrowWhenItemsAreMissing() {
        var command = new CreateLabelPrintJobCommand(LocalDate.now(), List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidLabelPrintJobException.class);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        var missingProductId = UUID.randomUUID();
        var command = new CreateLabelPrintJobCommand(
                LocalDate.now(),
                List.of(new CreateLabelPrintJobItemCommand(missingProductId, 1))
        );

        when(productRepository.findById(missingProductId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldThrowWhenProductIsInactive() {
        var productId = UUID.randomUUID();
        var command = new CreateLabelPrintJobCommand(
                LocalDate.now(),
                List.of(new CreateLabelPrintJobItemCommand(productId, 1))
        );

        var product = createMinimalProduct(productId, "SKU-001", "Produto A");
        product.deactivate();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidLabelPrintJobException.class);
    }

    private Product createMinimalProduct(UUID id, String sku, String name) {
        var product = Product.create(
                sku,
                null,
                name,
                null,
                null,
                null,
                null,
                "UN",
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
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
                null,
                null
        );
        product.setId(id);
        return product;
    }
}
