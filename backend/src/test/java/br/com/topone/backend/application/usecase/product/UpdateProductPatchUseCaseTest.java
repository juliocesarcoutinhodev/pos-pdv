package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.ProductBarcodeAlreadyExistsException;
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
class UpdateProductPatchUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UpdateProductPatchUseCase useCase;

    @Test
    void shouldPatchProductAndDeactivate() {
        var productId = UUID.randomUUID();
        var product = Product.create(
                "SKU-001",
                "7890001112223",
                "Produto XPTO",
                "Descricao antiga",
                "Marca antiga",
                "Categoria antiga",
                "UN",
                new BigDecimal("10.00"),
                new BigDecimal("15.00"),
                null,
                new BigDecimal("4"),
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
                "image-old"
        );
        product.setId(productId);
        product.setCreatedAt(Instant.now());

        var command = new UpdateProductPatchCommand(
                productId,
                null,
                "789.555.444.333-2",
                " Produto Atualizado ",
                null,
                null,
                " Categoria Nova ",
                null,
                null,
                new BigDecimal("17.9"),
                null,
                new BigDecimal("7.2"),
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
                " image-new ",
                false
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.findBySkuExcludingId("SKU-001", productId)).thenReturn(Optional.empty());
        when(productRepository.findByBarcodeExcludingId("7895554443332", productId)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            var updated = invocation.getArgument(0, Product.class);
            updated.setUpdatedAt(Instant.now());
            return updated;
        });

        var result = useCase.execute(command);

        assertThat(result.name()).isEqualTo("Produto Atualizado");
        assertThat(result.category()).isEqualTo("Categoria Nova");
        assertThat(result.salePrice()).isEqualByComparingTo("17.90");
        assertThat(result.stockQuantity()).isEqualByComparingTo("7.200");
        assertThat(result.barcode()).isEqualTo("7895554443332");
        assertThat(result.imageId()).isEqualTo("image-new");
        assertThat(result.active()).isFalse();
    }

    @Test
    void shouldThrowWhenBarcodeAlreadyExistsForAnotherProduct() {
        var productId = UUID.randomUUID();
        var product = Product.create(
                "SKU-001",
                "7890001112223",
                "Produto XPTO",
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

        var command = new UpdateProductPatchCommand(
                productId,
                null,
                "7891234567890",
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
                "SKU-002",
                "7891234567890",
                "Outro produto",
                null,
                null,
                null,
                "UN",
                null,
                new BigDecimal("20.00"),
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
        when(productRepository.findBySkuExcludingId("SKU-001", productId)).thenReturn(Optional.empty());
        when(productRepository.findByBarcodeExcludingId("7891234567890", productId))
                .thenReturn(Optional.of(anotherProduct));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductBarcodeAlreadyExistsException.class);

        verify(productRepository, never()).save(any());
    }
}
