package br.com.topone.backend.application.usecase.customer;

import java.util.UUID;

public record UpdateCustomerPatchCommand(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        CustomerAddressPatchCommand address,
        String imageId,
        Boolean active
) {
}
