package br.com.topone.backend.application.usecase.customer;

import br.com.topone.backend.domain.exception.CustomerTaxIdAlreadyExistsException;
import br.com.topone.backend.domain.model.Customer;
import br.com.topone.backend.domain.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCustomerUseCaseTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CreateCustomerUseCase useCase;

    @Test
    void shouldNormalizeAndCreateCustomer() {
        var command = new CreateCustomerCommand(
                "  Cliente XPTO  ",
                "37.335.118/0001-80",
                "  CLIENTE@EMAIL.COM  ",
                " 11 99999-9999 ",
                new CustomerAddressCommand(
                        "03195-000",
                        " Rua do Oratório ",
                        "100",
                        " Sala 2 ",
                        " Alto da Mooca ",
                        " São Paulo ",
                        "sp"
                ),
                LocalDate.parse("1990-10-20"),
                "feminino",
                "RG-1234567",
                "image-123"
        );

        when(customerRepository.existsByTaxId("37335118000180")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            var customer = invocation.getArgument(0, Customer.class);
            customer.setId(UUID.randomUUID());
            customer.setCreatedAt(Instant.now());
            return customer;
        });

        var result = useCase.execute(command);

        assertThat(result.name()).isEqualTo("Cliente XPTO");
        assertThat(result.taxId()).isEqualTo("37335118000180");
        assertThat(result.email()).isEqualTo("cliente@email.com");
        assertThat(result.address().zipCode()).isEqualTo("03195000");
        assertThat(result.address().state()).isEqualTo("SP");
        assertThat(result.birthDate()).isEqualTo(LocalDate.parse("1990-10-20"));
        assertThat(result.gender()).isEqualTo("FEMININO");
        assertThat(result.ieOrRg()).isEqualTo("RG-1234567");
        assertThat(result.imageId()).isEqualTo("image-123");
    }

    @Test
    void shouldThrowWhenTaxIdAlreadyExists() {
        var command = new CreateCustomerCommand(
                "Cliente XPTO",
                "37335118000180",
                "cliente@email.com",
                "11999999999",
                new CustomerAddressCommand("03195000", "Rua A", "10", null, "Centro", "São Paulo", "SP"),
                null,
                null,
                null,
                null
        );

        when(customerRepository.existsByTaxId("37335118000180")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(CustomerTaxIdAlreadyExistsException.class);
    }
}
