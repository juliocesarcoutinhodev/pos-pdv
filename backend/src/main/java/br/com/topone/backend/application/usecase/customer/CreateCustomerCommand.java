package br.com.topone.backend.application.usecase.customer;

import java.time.LocalDate;

public record CreateCustomerCommand(
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
