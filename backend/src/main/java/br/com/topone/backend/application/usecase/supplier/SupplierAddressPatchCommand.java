package br.com.topone.backend.application.usecase.supplier;

public record SupplierAddressPatchCommand(
        String zipCode,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state
) {
}
