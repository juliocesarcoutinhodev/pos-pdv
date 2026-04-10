package br.com.topone.backend.interfaces.dto;

import java.util.UUID;

public record AddressResponse(
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
