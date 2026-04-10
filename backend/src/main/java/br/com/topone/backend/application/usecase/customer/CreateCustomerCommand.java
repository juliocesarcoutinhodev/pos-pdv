package br.com.topone.backend.application.usecase.customer;

public record CreateCustomerCommand(
        String name,
        String taxId,
        String email,
        String phone,
        CustomerAddressCommand address,
        String imageId
) {
}
