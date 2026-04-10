package br.com.topone.backend.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateCustomerPatchRequest(
        @Size(max = 150) String name,
        @Size(max = 18) String taxId,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String phone,
        @Valid AddressPatchRequest address,
        @Size(max = 120) String imageId,
        Boolean active
) {
}
