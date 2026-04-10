package br.com.topone.backend.application.usecase.customer;

import br.com.topone.backend.domain.exception.CustomerTaxIdAlreadyExistsException;
import br.com.topone.backend.domain.model.Address;
import br.com.topone.backend.domain.model.Customer;
import br.com.topone.backend.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerPatchUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private UpdateCustomerPatchUseCase useCase;

    @Test
    void shouldPatchAddressAndDeactivateCustomer() {
        var customerId = UUID.randomUUID();
        var customer = Customer.create(
                "Cliente XPTO",
                "37335118000180",
                "cliente@email.com",
                "11999999999",
                LocalDate.parse("1985-06-15"),
                "MASCULINO",
                "RG-1111111",
                "image-old",
                Address.create("03195000", "Rua do Oratório", "100", null, "Alto da Mooca", "São Paulo", "SP")
        );
        customer.setId(customerId);
        customer.setCreatedAt(Instant.now());

        var command = new UpdateCustomerPatchCommand(
                customerId,
                null,
                null,
                null,
                null,
                new CustomerAddressPatchCommand(
                        null,
                        null,
                        null,
                        "Sala 2",
                        null,
                        "Campinas",
                        null
                ),
                LocalDate.parse("1986-07-20"),
                "feminino",
                "RG-2222222",
                "image-new",
                false
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            var updated = invocation.getArgument(0, Customer.class);
            updated.setUpdatedAt(Instant.now());
            return updated;
        });

        var result = useCase.execute(command);

        assertThat(result.active()).isFalse();
        assertThat(result.address().city()).isEqualTo("Campinas");
        assertThat(result.address().complement()).isEqualTo("Sala 2");
        assertThat(result.address().street()).isEqualTo("Rua do Oratório");
        assertThat(result.birthDate()).isEqualTo(LocalDate.parse("1986-07-20"));
        assertThat(result.gender()).isEqualTo("FEMININO");
        assertThat(result.ieOrRg()).isEqualTo("RG-2222222");
        assertThat(result.imageId()).isEqualTo("image-new");
    }

    @Test
    void shouldThrowWhenTaxIdAlreadyExistsForAnotherCustomer() {
        var customerId = UUID.randomUUID();
        var customer = Customer.create(
                "Cliente XPTO",
                "37335118000180",
                "cliente@email.com",
                "11999999999",
                null,
                null,
                null,
                null,
                Address.create("03195000", "Rua do Oratório", "100", null, "Alto da Mooca", "São Paulo", "SP")
        );
        customer.setId(customerId);

        var command = new UpdateCustomerPatchCommand(
                customerId,
                null,
                "11.222.333/0001-44",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        var anotherCustomer = Customer.create(
                "Outro cliente",
                "11222333000144",
                "outro@cliente.com",
                null,
                null,
                null,
                null,
                null,
                Address.create("01001000", "Rua B", "20", null, "Centro", "São Paulo", "SP")
        );
        anotherCustomer.setId(UUID.randomUUID());

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.findByTaxIdExcludingId("11222333000144", customerId))
                .thenReturn(Optional.of(anotherCustomer));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CustomerTaxIdAlreadyExistsException.class);

        verify(customerRepository, never()).save(any());
    }
}
