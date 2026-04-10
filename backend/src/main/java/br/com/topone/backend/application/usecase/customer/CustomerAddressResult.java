package br.com.topone.backend.application.usecase.customer;

import java.util.UUID;

public record CustomerAddressResult(
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
