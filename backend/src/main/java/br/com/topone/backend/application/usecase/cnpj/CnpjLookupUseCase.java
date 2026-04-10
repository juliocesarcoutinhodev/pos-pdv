package br.com.topone.backend.application.usecase.cnpj;

import br.com.topone.backend.domain.exception.InvalidCnpjException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case for querying CNPJ data from the configured provider.
 */
@Service
@RequiredArgsConstructor
public class CnpjLookupUseCase {

    private final CnpjLookupGateway cnpjLookupGateway;

    public CnpjLookupResult execute(String taxId) {
        var normalizedTaxId = normalizeTaxId(taxId);
        validateTaxId(normalizedTaxId);
        return cnpjLookupGateway.findByTaxId(normalizedTaxId);
    }

    private String normalizeTaxId(String taxId) {
        if (taxId == null) {
            throw new InvalidCnpjException();
        }
        return taxId.replaceAll("\\D", "");
    }

    private void validateTaxId(String taxId) {
        if (taxId.length() != 14) {
            throw new InvalidCnpjException();
        }
    }
}

