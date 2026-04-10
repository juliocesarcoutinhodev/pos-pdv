package br.com.topone.backend.infrastructure.external.cnpja;

import br.com.topone.backend.application.usecase.cnpj.CnpjLookupResult;
import br.com.topone.backend.domain.exception.CnpjNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CnpjaLookupGatewayAdapterTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private CnpjaLookupGatewayAdapter adapter;

    @BeforeEach
    void setUp() {
        var cacheProperties = new LookupCacheProperties();
        cacheProperties.setEnabled(true);
        adapter = new CnpjaLookupGatewayAdapter(restClient, cacheProperties);
    }

    @Test
    void shouldCacheSuccessfulLookup() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CnpjLookupResult.class)).thenReturn(sampleResult());

        var firstResult = adapter.findByTaxId("37335118000180");
        var secondResult = adapter.findByTaxId("37335118000180");

        assertThat(firstResult.taxId()).isEqualTo("37335118000180");
        assertThat(secondResult).isEqualTo(firstResult);
        verify(restClient, times(1)).get();
    }

    @Test
    void shouldCacheNotFoundResult() {
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CnpjLookupResult.class)).thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> adapter.findByTaxId("00000000000000"))
                .isInstanceOf(CnpjNotFoundException.class);
        assertThatThrownBy(() -> adapter.findByTaxId("00000000000000"))
                .isInstanceOf(CnpjNotFoundException.class);

        verify(restClient, times(1)).get();
    }

    private CnpjLookupResult sampleResult() {
        return new CnpjLookupResult(
                null,
                "37335118000180",
                "CNPJA TECNOLOGIA LTDA",
                "Cnpja",
                null,
                null,
                true,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                List.of(),
                List.of()
        );
    }
}
