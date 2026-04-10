package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.zip.ZipLookupResult;
import br.com.topone.backend.application.usecase.zip.ZipLookupUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZipControllerTest {

    @Mock
    private ZipLookupUseCase zipLookupUseCase;

    @InjectMocks
    private ZipController controller;

    @Test
    void shouldReturnZipDataFromUseCase() {
        when(zipLookupUseCase.execute("03195000")).thenReturn(sampleResult());

        var response = controller.lookup("03195000");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("03195000");
        assertThat(response.getBody().city()).isEqualTo("São Paulo");
    }

    private ZipLookupResult sampleResult() {
        return new ZipLookupResult(
                OffsetDateTime.parse("2026-03-25T03:00:00Z"),
                "03195000",
                3550308,
                "Rua do Oratório",
                null,
                "Alto da Mooca",
                "São Paulo",
                "SP"
        );
    }
}

