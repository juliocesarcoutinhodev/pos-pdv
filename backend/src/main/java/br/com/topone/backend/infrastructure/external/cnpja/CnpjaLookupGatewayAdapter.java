package br.com.topone.backend.infrastructure.external.cnpja;

import br.com.topone.backend.application.usecase.cnpj.CnpjLookupGateway;
import br.com.topone.backend.application.usecase.cnpj.CnpjLookupResult;
import br.com.topone.backend.domain.exception.CnpjNotFoundException;
import br.com.topone.backend.domain.exception.CnpjaIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * HTTP adapter that calls CNPJA provider API.
 */
@Slf4j
@Service
public class CnpjaLookupGatewayAdapter implements CnpjLookupGateway {

    private final RestClient restClient;

    public CnpjaLookupGatewayAdapter(CnpjaProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, properties.getToken())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public CnpjLookupResult findByTaxId(String taxId) {
        try {
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/rfb").queryParam("taxId", taxId).build())
                    .retrieve()
                    .body(CnpjLookupResult.class);

            if (response == null) {
                throw new CnpjaIntegrationException("Provider returned empty payload");
            }
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new CnpjNotFoundException();
            }
            log.error("CNPJA request failed | status={} | body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new CnpjaIntegrationException("Failed to fetch CNPJ from provider");
        } catch (CnpjaIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("CNPJA request failed unexpectedly", ex);
            throw new CnpjaIntegrationException("Failed to fetch CNPJ from provider");
        }
    }
}
