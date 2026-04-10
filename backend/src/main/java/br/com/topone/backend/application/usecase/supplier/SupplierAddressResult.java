package br.com.topone.backend.application.usecase.supplier;

import java.util.UUID;

public record SupplierAddressResult(
        UUID id,
        String zipCode,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state
) {
}
