package br.com.topone.backend.application.usecase.supplier;

import br.com.topone.backend.domain.exception.SupplierNotFoundException;
import br.com.topone.backend.domain.model.Address;
import br.com.topone.backend.domain.model.Supplier;
import br.com.topone.backend.domain.repository.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class DeactivateSupplierUseCaseTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private DeactivateSupplierUseCase useCase;

    @Test
    void shouldThrowWhenSupplierNotFound() {
        var supplierId = UUID.randomUUID();
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new DeactivateSupplierCommand(supplierId)))
                .isInstanceOf(SupplierNotFoundException.class);

        verify(supplierRepository, never()).save(any());
    }

    @Test
    void shouldDeactivateSupplier() {
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

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0, Supplier.class));

        var result = useCase.execute(new DeactivateSupplierCommand(supplierId));

        assertThat(result.id()).isEqualTo(supplierId);
        assertThat(supplier.isActive()).isFalse();
        verify(supplierRepository).save(supplier);
    }
}
