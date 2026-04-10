package br.com.topone.backend.application.usecase.supplier;

public record SupplierAddressCommand(
        String zipCode,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state
) {
}
