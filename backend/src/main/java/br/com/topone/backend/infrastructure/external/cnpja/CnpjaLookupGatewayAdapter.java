package br.com.topone.backend.infrastructure.external.cnpja;

import br.com.topone.backend.application.usecase.cnpj.CnpjLookupGateway;
import br.com.topone.backend.application.usecase.cnpj.CnpjLookupResult;
import br.com.topone.backend.domain.exception.CnpjNotFoundException;
import br.com.topone.backend.domain.exception.CnpjaIntegrationException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.concurrent.TimeUnit;

/**
 * HTTP adapter that calls CNPJA provider API.
 */
@Slf4j
@Service
public class CnpjaLookupGatewayAdapter implements CnpjLookupGateway {

    private final RestClient restClient;
    private final boolean cacheEnabled;
    private final Cache<String, CnpjLookupResult> successCache;
    private final Cache<String, Boolean> notFoundCache;

    @Autowired
    public CnpjaLookupGatewayAdapter(CnpjaProperties properties, LookupCacheProperties lookupCacheProperties) {
        this(createRestClient(properties), lookupCacheProperties);
    }

    CnpjaLookupGatewayAdapter(RestClient restClient, LookupCacheProperties lookupCacheProperties) {
        this.restClient = restClient;
        var cacheConfig = lookupCacheProperties.getCnpj();
        this.cacheEnabled = lookupCacheProperties.isEnabled();
        this.successCache = Caffeine.newBuilder()
                .expireAfterWrite(cacheConfig.getSuccessTtlMinutes(), TimeUnit.MINUTES)
                .maximumSize(cacheConfig.getMaximumSize())
                .build();
        this.notFoundCache = Caffeine.newBuilder()
                .expireAfterWrite(cacheConfig.getNotFoundTtlMinutes(), TimeUnit.MINUTES)
                .maximumSize(cacheConfig.getMaximumSize())
                .build();
    }

    private static RestClient createRestClient(CnpjaProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, properties.getToken())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public CnpjLookupResult findByTaxId(String taxId) {
        var cachedResult = getCachedResult(taxId);
        if (cachedResult != null) {
            return cachedResult;
        }

        if (isNotFoundCached(taxId)) {
            throw new CnpjNotFoundException();
        }

        try {
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/rfb").queryParam("taxId", taxId).build())
                    .retrieve()
                    .body(CnpjLookupResult.class);

            if (response == null) {
                throw new CnpjaIntegrationException("Provider returned empty payload");
            }

            cacheSuccess(taxId, response);
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                cacheNotFound(taxId);
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

    private CnpjLookupResult getCachedResult(String taxId) {
        if (!cacheEnabled) {
            return null;
        }

        var cached = successCache.getIfPresent(taxId);
        if (cached != null) {
            log.debug("CNPJ cache hit | taxId={}", taxId);
        }
        return cached;
    }

    private boolean isNotFoundCached(String taxId) {
        if (!cacheEnabled) {
            return false;
        }

        var cached = notFoundCache.getIfPresent(taxId);
        if (Boolean.TRUE.equals(cached)) {
            log.debug("CNPJ negative cache hit | taxId={}", taxId);
            return true;
        }
        return false;
    }

    private void cacheSuccess(String taxId, CnpjLookupResult response) {
        if (!cacheEnabled) {
            return;
        }
        successCache.put(taxId, response);
        notFoundCache.invalidate(taxId);
    }

    private void cacheNotFound(String taxId) {
        if (!cacheEnabled) {
            return;
        }
        notFoundCache.put(taxId, Boolean.TRUE);
        successCache.invalidate(taxId);
    }
}
