package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 12) String zipCode,
        @NotBlank @Size(max = 255) String street,
        @Size(max = 20) String number,
        @Size(max = 120) String complement,
        @NotBlank @Size(max = 120) String district,
        @NotBlank @Size(max = 120) String city,
        @NotBlank @Size(min = 2, max = 2) String state
) {
}
