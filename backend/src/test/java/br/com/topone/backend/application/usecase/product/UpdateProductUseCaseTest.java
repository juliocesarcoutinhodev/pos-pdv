package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.ProductSkuAlreadyExistsException;
import br.com.topone.backend.domain.model.Product;
import br.com.topone.backend.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UpdateProductUseCase useCase;

    @Test
    void shouldUpdateProductWithNormalization() {
        var productId = UUID.randomUUID();
        var product = Product.create(
                "SKU-OLD",
                "7890000000001",
                "Produto antigo",
                "Descricao antiga",
                "Marca antiga",
                "Categoria antiga",
                "UN",
                new BigDecimal("10.00"),
                new BigDecimal("12.00"),
                null,
                new BigDecimal("3"),
                new BigDecimal("1"),
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
        product.setId(productId);
        product.setCreatedAt(Instant.now());

        var command = new UpdateProductCommand(
                productId,
                " sku-new ",
                "789.123.456.789-0",
                " Produto novo ",
                " Nova descricao ",
                " Nova marca ",
                " Nova categoria ",
                " kg ",
                new BigDecimal("15"),
                new BigDecimal("22"),
                new BigDecimal("20.1"),
                new BigDecimal("9.5"),
                new BigDecimal("2"),
                "22030090",
                "1234567",
                "5102",
                " 0 ",
                " 60 ",
                new BigDecimal("18"),
                " 01 ",
                new BigDecimal("1.65"),
                " 01 ",
                new BigDecimal("7.6"),
                " image-new "
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.findBySkuExcludingId("SKU-NEW", productId)).thenReturn(Optional.empty());
        when(productRepository.findByBarcodeExcludingId("7891234567890", productId)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            var updated = invocation.getArgument(0, Product.class);
            updated.setUpdatedAt(Instant.now());
            return updated;
        });

        var result = useCase.execute(command);

        assertThat(result.sku()).isEqualTo("SKU-NEW");
        assertThat(result.barcode()).isEqualTo("7891234567890");
        assertThat(result.name()).isEqualTo("Produto novo");
        assertThat(result.unit()).isEqualTo("KG");
        assertThat(result.salePrice()).isEqualByComparingTo("22.00");
        assertThat(result.promotionalPrice()).isEqualByComparingTo("20.10");
        assertThat(result.stockQuantity()).isEqualByComparingTo("9.500");
        assertThat(result.active()).isTrue();
    }

    @Test
    void shouldThrowWhenSkuAlreadyExistsForAnotherProduct() {
        var productId = UUID.randomUUID();
        var product = Product.create(
                "SKU-OLD",
                null,
                "Produto",
                null,
                null,
                null,
                "UN",
                null,
                new BigDecimal("10.00"),
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
        product.setId(productId);

        var command = new UpdateProductCommand(
                productId,
                "SKU-001",
                null,
                "Produto",
                null,
                null,
                null,
                "UN",
                null,
                new BigDecimal("10.00"),
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

        var anotherProduct = Product.create(
                "SKU-001",
                null,
                "Outro produto",
                null,
                null,
                null,
                "UN",
                null,
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
        anotherProduct.setId(UUID.randomUUID());

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.findBySkuExcludingId("SKU-001", productId)).thenReturn(Optional.of(anotherProduct));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductSkuAlreadyExistsException.class);

        verify(productRepository, never()).save(any());
    }
}
