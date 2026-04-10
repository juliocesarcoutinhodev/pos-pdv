package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.Size;

public record AddressPatchRequest(
        @Size(max = 12) String zipCode,
        @Size(max = 255) String street,
        @Size(max = 20) String number,
        @Size(max = 120) String complement,
        @Size(max = 120) String district,
        @Size(max = 120) String city,
        @Size(min = 2, max = 2) String state
) {
}
