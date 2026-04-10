package br.com.topone.backend.application.usecase.zip;

import br.com.topone.backend.domain.exception.InvalidZipCodeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Use case for querying ZIP code data from the configured provider.
 */
@Service
@RequiredArgsConstructor
public class ZipLookupUseCase {

    private final ZipLookupGateway zipLookupGateway;

    public ZipLookupResult execute(String code) {
        var normalizedCode = normalizeCode(code);
        validateCode(normalizedCode);
        return zipLookupGateway.findByCode(normalizedCode);
    }

    private String normalizeCode(String code) {
        if (code == null) {
            throw new InvalidZipCodeException();
        }
        return code.replaceAll("\\D", "");
    }

    private void validateCode(String code) {
        if (code.length() != 8) {
            throw new InvalidZipCodeException();
        }
    }
}

