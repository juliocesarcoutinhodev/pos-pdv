package br.com.topone.backend.application.usecase.zip;

import br.com.topone.backend.domain.exception.InvalidZipCodeException;
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
class ZipLookupUseCaseTest {

    @Mock
    private ZipLookupGateway zipLookupGateway;

    @InjectMocks
    private ZipLookupUseCase useCase;

    @Test
    void shouldNormalizeAndLookupCode() {
        var code = "03195-000";
        when(zipLookupGateway.findByCode("03195000")).thenReturn(null);

        useCase.execute(code);

        verify(zipLookupGateway).findByCode("03195000");
    }

    @Test
    void shouldThrowWhenCodeIsInvalid() {
        assertThatThrownBy(() -> useCase.execute("123"))
                .isInstanceOf(InvalidZipCodeException.class);

        verifyNoInteractions(zipLookupGateway);
    }
}

