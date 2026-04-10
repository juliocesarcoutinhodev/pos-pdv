package br.com.topone.backend.infrastructure.external.cnpja;

import br.com.topone.backend.application.usecase.zip.ZipLookupGateway;
import br.com.topone.backend.application.usecase.zip.ZipLookupResult;
import br.com.topone.backend.domain.exception.ZipCodeNotFoundException;
import br.com.topone.backend.domain.exception.ZipIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * HTTP adapter that calls CNPJA ZIP endpoint.
 */
@Slf4j
@Service
public class CnpjaZipLookupGatewayAdapter implements ZipLookupGateway {

    private final RestClient restClient;

    public CnpjaZipLookupGatewayAdapter(CnpjaProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.getUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, properties.getToken())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public ZipLookupResult findByCode(String code) {
        try {
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/zip/{code}").build(code))
                    .retrieve()
                    .body(ZipLookupResult.class);

            if (response == null) {
                throw new ZipIntegrationException("Provider returned empty payload");
            }
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ZipCodeNotFoundException();
            }
            log.error("CNPJA ZIP request failed | status={} | body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new ZipIntegrationException("Failed to fetch ZIP code from provider");
        } catch (ZipIntegrationException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("CNPJA ZIP request failed unexpectedly", ex);
            throw new ZipIntegrationException("Failed to fetch ZIP code from provider");
        }
    }
}

