package br.com.topone.backend.application.usecase.supplier;

import br.com.topone.backend.domain.exception.SupplierTaxIdAlreadyExistsException;
import br.com.topone.backend.domain.model.Address;
import br.com.topone.backend.domain.model.Contact;
import br.com.topone.backend.domain.model.Supplier;
import br.com.topone.backend.domain.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateSupplierPatchUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private UpdateSupplierPatchUseCase useCase;

    @Test
    void shouldPatchAddressAndDeactivateSupplier() {
        var supplierId = UUID.randomUUID();
        var supplier = Supplier.create(
                "Fornecedor XPTO",
                "37335118000180",
                "contato@fornecedor.com",
                "11999999999",
                Address.create("03195000", "Rua do Oratório", "100", null, "Alto da Mooca", "São Paulo", "SP"),
                List.of(Contact.create("Maria", "maria@fornecedor.com", "11999999999"))
        );
        supplier.setId(supplierId);
        supplier.setCreatedAt(Instant.now());

        var command = new UpdateSupplierPatchCommand(
                supplierId,
                null,
                null,
                null,
                null,
                new SupplierAddressPatchCommand(
                        null,
                        null,
                        null,
                        "Sala 2",
                        null,
                        "Campinas",
                        null
                ),
                List.of(new SupplierContactCommand("Carlos", "carlos@fornecedor.com", "11988887777")),
                false
        );

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            var updated = invocation.getArgument(0, Supplier.class);
            updated.setUpdatedAt(Instant.now());
            return updated;
        });

        var result = useCase.execute(command);

        assertThat(result.active()).isFalse();
        assertThat(result.address().city()).isEqualTo("Campinas");
        assertThat(result.address().complement()).isEqualTo("Sala 2");
        assertThat(result.address().street()).isEqualTo("Rua do Oratório");
        assertThat(result.contacts()).hasSize(1);
        assertThat(result.contacts().get(0).name()).isEqualTo("Carlos");
    }

    @Test
    void shouldThrowWhenTaxIdAlreadyExistsForAnotherSupplier() {
        var supplierId = UUID.randomUUID();
        var supplier = Supplier.create(
                "Fornecedor XPTO",
                "37335118000180",
                "contato@fornecedor.com",
                "11999999999",
                Address.create("03195000", "Rua do Oratório", "100", null, "Alto da Mooca", "São Paulo", "SP"),
                List.of()
        );
        supplier.setId(supplierId);

        var command = new UpdateSupplierPatchCommand(
                supplierId,
                null,
                "11.222.333/0001-44",
                null,
                null,
                null,
                null,
                null
        );

        var anotherSupplier = Supplier.create(
                "Outro fornecedor",
                "11222333000144",
                "outro@fornecedor.com",
                null,
                Address.create("01001000", "Rua B", "20", null, "Centro", "São Paulo", "SP"),
                List.of()
        );
        anotherSupplier.setId(UUID.randomUUID());

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.findByTaxIdExcludingId("11222333000144", supplierId))
                .thenReturn(Optional.of(anotherSupplier));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(SupplierTaxIdAlreadyExistsException.class);

        verify(supplierRepository, never()).save(any());
    }
}
