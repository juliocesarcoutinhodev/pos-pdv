package br.com.topone.backend.application.usecase.zip;

import java.time.OffsetDateTime;

/**
 * Canonical ZIP code lookup payload returned by the application layer.
 */
public record ZipLookupResult(
        OffsetDateTime updated,
        String code,
        Integer municipality,
        String street,
        String number,
        String district,
        String city,
        String state
) {
}

