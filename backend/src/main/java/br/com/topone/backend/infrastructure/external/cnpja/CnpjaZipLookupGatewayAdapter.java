package br.com.topone.backend.infrastructure.external.cnpja;

import br.com.topone.backend.application.usecase.zip.ZipLookupGateway;
import br.com.topone.backend.application.usecase.zip.ZipLookupResult;
import br.com.topone.backend.domain.exception.ZipCodeNotFoundException;
import br.com.topone.backend.domain.exception.ZipIntegrationException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.concurrent.TimeUnit;

/**
 * HTTP adapter that calls CNPJA ZIP endpoint.
 */
@Slf4j
@Service
public class CnpjaZipLookupGatewayAdapter implements ZipLookupGateway {

    private final RestClient restClient;
    private final boolean cacheEnabled;
    private final Cache<String, ZipLookupResult> successCache;
    private final Cache<String, Boolean> notFoundCache;

    @Autowired
    public CnpjaZipLookupGatewayAdapter(CnpjaProperties properties, LookupCacheProperties lookupCacheProperties) {
        this(createRestClient(properties), lookupCacheProperties);
    }

    CnpjaZipLookupGatewayAdapter(RestClient restClient, LookupCacheProperties lookupCacheProperties) {
        this.restClient = restClient;
        var cacheConfig = lookupCacheProperties.getZip();
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
    public ZipLookupResult findByCode(String code) {
        var cachedResult = getCachedResult(code);
        if (cachedResult != null) {
            return cachedResult;
        }

        if (isNotFoundCached(code)) {
            throw new ZipCodeNotFoundException();
        }

        try {
            var response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/zip/{code}").build(code))
                    .retrieve()
                    .body(ZipLookupResult.class);

            if (response == null) {
                throw new ZipIntegrationException("Provider returned empty payload");
            }

            cacheSuccess(code, response);
            return response;
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                cacheNotFound(code);
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

    private ZipLookupResult getCachedResult(String code) {
        if (!cacheEnabled) {
            return null;
        }

        var cached = successCache.getIfPresent(code);
        if (cached != null) {
            log.debug("ZIP cache hit | code={}", code);
        }
        return cached;
    }

    private boolean isNotFoundCached(String code) {
        if (!cacheEnabled) {
            return false;
        }

        var cached = notFoundCache.getIfPresent(code);
        if (Boolean.TRUE.equals(cached)) {
            log.debug("ZIP negative cache hit | code={}", code);
            return true;
        }
        return false;
    }

    private void cacheSuccess(String code, ZipLookupResult response) {
        if (!cacheEnabled) {
            return;
        }
        successCache.put(code, response);
        notFoundCache.invalidate(code);
    }

    private void cacheNotFound(String code) {
        if (!cacheEnabled) {
            return;
        }
        notFoundCache.put(code, Boolean.TRUE);
        successCache.invalidate(code);
    }
}
