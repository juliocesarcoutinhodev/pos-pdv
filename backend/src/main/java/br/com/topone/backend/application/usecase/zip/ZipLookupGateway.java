package br.com.topone.backend.application.usecase.zip;

/**
 * Outbound gateway for ZIP code provider lookups.
 */
public interface ZipLookupGateway {

    ZipLookupResult findByCode(String code);
}

