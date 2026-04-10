package br.com.topone.backend.application.usecase.customer;

import java.util.UUID;

public record UpdateCustomerCommand(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        CustomerAddressCommand address,
        String imageId
) {
}
