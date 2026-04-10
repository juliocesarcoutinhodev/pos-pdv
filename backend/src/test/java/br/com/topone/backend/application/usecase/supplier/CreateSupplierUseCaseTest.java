package br.com.topone.backend.application.usecase.supplier;

import br.com.topone.backend.domain.exception.SupplierTaxIdAlreadyExistsException;
import br.com.topone.backend.domain.model.Supplier;
import br.com.topone.backend.domain.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSupplierUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private CreateSupplierUseCase useCase;

    @Test
    void shouldNormalizeAndCreateSupplier() {
        var command = new CreateSupplierCommand(
                "  Fornecedor XPTO  ",
                "37.335.118/0001-80",
                "  CONTATO@FORNECEDOR.COM  ",
                " 11 99999-9999 ",
                new SupplierAddressCommand(
                        "03195-000",
                        " Rua do Oratório ",
                        "100",
                        " Sala 2 ",
                        " Alto da Mooca ",
                        " São Paulo ",
                        "sp"
                )
        );

        when(supplierRepository.existsByTaxId("37335118000180")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            var supplier = invocation.getArgument(0, Supplier.class);
            supplier.setId(UUID.randomUUID());
            supplier.setCreatedAt(Instant.now());
            return supplier;
        });

        var result = useCase.execute(command);

        assertThat(result.name()).isEqualTo("Fornecedor XPTO");
        assertThat(result.taxId()).isEqualTo("37335118000180");
        assertThat(result.email()).isEqualTo("contato@fornecedor.com");
        assertThat(result.address().zipCode()).isEqualTo("03195000");
        assertThat(result.address().state()).isEqualTo("SP");
        assertThat(result.address().street()).isEqualTo("Rua do Oratório");
    }

    @Test
    void shouldThrowWhenTaxIdAlreadyExists() {
        var command = new CreateSupplierCommand(
                "Fornecedor XPTO",
                "37335118000180",
                "contato@fornecedor.com",
                "11999999999",
                new SupplierAddressCommand("03195000", "Rua A", "10", null, "Centro", "São Paulo", "SP")
        );

        when(supplierRepository.existsByTaxId("37335118000180")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SupplierTaxIdAlreadyExistsException.class);
    }
}
