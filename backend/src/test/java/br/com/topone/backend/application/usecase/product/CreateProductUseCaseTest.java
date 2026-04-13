package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.ProductBarcodeAlreadyExistsException;
import br.com.topone.backend.domain.exception.ProductSkuAlreadyExistsException;
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
class CreateProductUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private CreateProductUseCase useCase;

    @Test
    void shouldNormalizeAndCreateProduct() {
        var command = new CreateProductCommand(
                " sku-001 ",
                " 789.000.111.222-3 ",
                " Produto XPTO ",
                " Descricao do produto ",
                " Marca A ",
                " Categoria B ",
                null,
                " un ",
                new BigDecimal("10.5"),
                new BigDecimal("25"),
                new BigDecimal("21.998"),
                new BigDecimal("5.1"),
                new BigDecimal("1"),
                "2203.00.90",
                "12.34567",
                "5102",
                " 0 ",
                " 60 ",
                new BigDecimal("18"),
                " 01 ",
                new BigDecimal("1.65"),
                " 01 ",
                new BigDecimal("7.60"),
                " image-123 "
        );

        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productRepository.existsByBarcode("7890001112223")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            var product = invocation.getArgument(0, Product.class);
            product.setId(UUID.randomUUID());
            product.setCreatedAt(Instant.now());
            return product;
        });

        var result = useCase.execute(command);

        assertThat(result.sku()).isEqualTo("SKU-001");
        assertThat(result.barcode()).isEqualTo("7890001112223");
        assertThat(result.name()).isEqualTo("Produto XPTO");
        assertThat(result.description()).isEqualTo("Descricao do produto");
        assertThat(result.brand()).isEqualTo("Marca A");
        assertThat(result.category()).isEqualTo("Categoria B");
        assertThat(result.unit()).isEqualTo("UN");
        assertThat(result.costPrice()).isEqualByComparingTo("10.50");
        assertThat(result.salePrice()).isEqualByComparingTo("25.00");
        assertThat(result.promotionalPrice()).isEqualByComparingTo("22.00");
        assertThat(result.stockQuantity()).isEqualByComparingTo("5.100");
        assertThat(result.minimumStock()).isEqualByComparingTo("1.000");
        assertThat(result.ncm()).isEqualTo("22030090");
        assertThat(result.cest()).isEqualTo("1234567");
        assertThat(result.taxOrigin()).isEqualTo("0");
        assertThat(result.taxSituation()).isEqualTo("60");
        assertThat(result.pisSituation()).isEqualTo("01");
        assertThat(result.cofinsSituation()).isEqualTo("01");
        assertThat(result.imageId()).isEqualTo("image-123");
    }

    @Test
    void shouldThrowWhenSkuAlreadyExists() {
        var command = new CreateProductCommand(
                "SKU-001",
                null,
                "Produto XPTO",
                null,
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

        when(productRepository.existsBySku("SKU-001")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductSkuAlreadyExistsException.class);
    }

    @Test
    void shouldThrowWhenBarcodeAlreadyExists() {
        var command = new CreateProductCommand(
                "SKU-001",
                "7890001112223",
                "Produto XPTO",
                null,
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

        when(productRepository.existsBySku("SKU-001")).thenReturn(false);
        when(productRepository.existsByBarcode("7890001112223")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(ProductBarcodeAlreadyExistsException.class);
    }

    @Test
    void shouldThrowWhenSupplierDoesNotExist() {
        var supplierId = UUID.randomUUID();
        var command = new CreateProductCommand(
                "SKU-001",
                null,
                "Produto XPTO",
                null,
                null,
                null,
                supplierId,
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

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SupplierNotFoundException.class);

        verify(productRepository, never()).save(any());
    }
}
