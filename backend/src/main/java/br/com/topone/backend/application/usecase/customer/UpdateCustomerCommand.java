package br.com.topone.backend.application.usecase.customer;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateCustomerCommand(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        CustomerAddressCommand address,
        LocalDate birthDate,
        String gender,
        String ieOrRg,
        String imageId
) {
}
