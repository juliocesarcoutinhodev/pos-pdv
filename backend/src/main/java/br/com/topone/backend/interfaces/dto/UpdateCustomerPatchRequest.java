package br.com.topone.backend.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateCustomerPatchRequest(
        @Size(max = 150) String name,
        @Size(max = 18) String taxId,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String phone,
        @Valid AddressPatchRequest address,
        LocalDate birthDate,
        @Size(max = 40) String gender,
        @Size(max = 30) String ieOrRg,
        @Size(max = 120) String imageId,
        Boolean active
) {
}
