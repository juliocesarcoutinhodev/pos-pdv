package br.com.topone.backend.infrastructure.external.cnpja;

import br.com.topone.backend.application.usecase.zip.ZipLookupResult;
import br.com.topone.backend.domain.exception.ZipCodeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CnpjaZipLookupGatewayAdapterTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private CnpjaZipLookupGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        var cacheProperties = new LookupCacheProperties();
        cacheProperties.setEnabled(true);
        adapter = new CnpjaZipLookupGatewayAdapter(restClient, cacheProperties);
    }

    @Test
    void shouldCacheSuccessfulLookup() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ZipLookupResult.class)).thenReturn(sampleResult());

        var firstResult = adapter.findByCode("03195000");
        var secondResult = adapter.findByCode("03195000");

        assertThat(firstResult.code()).isEqualTo("03195000");
        assertThat(secondResult).isEqualTo(firstResult);
        verify(restClient, times(1)).get();
    }

    @Test
    void shouldCacheNotFoundResult() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ZipLookupResult.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> adapter.findByCode("00000000"))
                .isInstanceOf(ZipCodeNotFoundException.class);
        assertThatThrownBy(() -> adapter.findByCode("00000000"))
                .isInstanceOf(ZipCodeNotFoundException.class);

        verify(restClient, times(1)).get();
    }

    private ZipLookupResult sampleResult() {
        return new ZipLookupResult(
                null,
                "03195000",
                3550308,
                "Rua do Oratório",
                "100",
                "Alto da Mooca",
                "São Paulo",
                "SP"
        );
    }
}
