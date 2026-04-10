package br.com.topone.backend.application.usecase.cnpj;

/**
 * Outbound gateway for CNPJ provider lookups.
 */
public interface CnpjLookupGateway {

    CnpjLookupResult findByTaxId(String taxId);
}

