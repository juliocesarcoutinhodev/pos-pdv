package br.com.topone.backend.application.usecase.cnpj;

import br.com.topone.backend.domain.exception.InvalidCnpjException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CnpjLookupUseCaseTest {

    @Mock
    private CnpjLookupGateway cnpjLookupGateway;

    @InjectMocks
    private CnpjLookupUseCase useCase;

    @Test
    void shouldNormalizeAndLookupTaxId() {
        var taxId = "37.335.118/0001-80";
        when(cnpjLookupGateway.findByTaxId("37335118000180")).thenReturn(null);

        useCase.execute(taxId);

        verify(cnpjLookupGateway).findByTaxId("37335118000180");
    }

    @Test
    void shouldThrowWhenTaxIdIsInvalid() {
        assertThatThrownBy(() -> useCase.execute("123"))
                .isInstanceOf(InvalidCnpjException.class);

        verifyNoInteractions(cnpjLookupGateway);
    }
}

