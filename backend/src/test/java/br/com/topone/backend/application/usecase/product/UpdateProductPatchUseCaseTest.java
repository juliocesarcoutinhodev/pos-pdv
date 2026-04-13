package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.InvalidProductPricingException;
import br.com.topone.backend.domain.exception.ProductBarcodeAlreadyExistsException;
import br.com.topone.backend.domain.exception.SupplierNotFoundException;
import br.com.topone.backend.domain.model.Product;
import br.com.topone.backend.domain.repository.ProductRepository;
import br.com.topone.backend.domain.repository.SupplierRepository;
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

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private UpdateProductPatchUseCase useCase;

    @Test
    void shouldPatchProductAndDeactivate() {
        var productId = UUID.randomUUID();
        var product = createMinimalProduct("SKU-001", "7890001112223", "Produto XPTO", new BigDecimal("10.00"), new BigDecimal("15.00"));
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
                null,
                new BigDecimal("17.90"),
                null,
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
        assertThat(result.marginPercentage()).isEqualByComparingTo("79.00");
        assertThat(result.stockQuantity()).isEqualByComparingTo("7.200");
        assertThat(result.barcode()).isEqualTo("7895554443332");
        assertThat(result.imageId()).isEqualTo("image-new");
        assertThat(result.active()).isFalse();
    }

    @Test
    void shouldCalculateSalePriceOnPatchWhenMarginIsInformed() {
        var productId = UUID.randomUUID();
        var product = createMinimalProduct("SKU-001", null, "Produto XPTO", new BigDecimal("20.00"), new BigDecimal("24.00"));
        product.setId(productId);
        product.setCreatedAt(Instant.now());

        var command = new UpdateProductPatchCommand(
                productId,
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
                new BigDecimal("25.00"),
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

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.findBySkuExcludingId("SKU-001", productId)).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0, Product.class));

        var result = useCase.execute(command);

        assertThat(result.salePrice()).isEqualByComparingTo("25.00");
        assertThat(result.marginPercentage()).isEqualByComparingTo("25.00");
    }

    @Test
    void shouldThrowWhenMarginIsInformedWithoutCostOnPatch() {
        var productId = UUID.randomUUID();
        var product = createMinimalProduct("SKU-001", null, "Produto XPTO", null, new BigDecimal("10.00"));
        product.setId(productId);

        var command = new UpdateProductPatchCommand(
                productId,
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
                null,
                null
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidProductPricingException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBarcodeAlreadyExistsForAnotherProduct() {
        var productId = UUID.randomUUID();
        var product = createMinimalProduct("SKU-001", "7890001112223", "Produto XPTO", null, new BigDecimal("10.00"));
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
                null,
                null,
                null
        );

        var anotherProduct = createMinimalProduct("SKU-002", "7891234567890", "Outro produto", null, new BigDecimal("20.00"));
        anotherProduct.setId(UUID.randomUUID());

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.findBySkuExcludingId("SKU-001", productId)).thenReturn(Optional.empty());
        when(productRepository.findByBarcodeExcludingId("7891234567890", productId))
                .thenReturn(Optional.of(anotherProduct));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductBarcodeAlreadyExistsException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSupplierDoesNotExist() {
        var productId = UUID.randomUUID();
        var supplierId = UUID.randomUUID();
        var product = createMinimalProduct("SKU-001", null, "Produto XPTO", null, new BigDecimal("10.00"));
        product.setId(productId);

        var command = new UpdateProductPatchCommand(
                productId,
                null,
                null,
                null,
                null,
                null,
                null,
                supplierId,
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

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SupplierNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    private Product createMinimalProduct(String sku, String barcode, String name, BigDecimal costPrice, BigDecimal salePrice) {
        return Product.create(
                sku,
                barcode,
                name,
                null,
                null,
                null,
                null,
                "UN",
                costPrice,
                salePrice,
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
    }
}
