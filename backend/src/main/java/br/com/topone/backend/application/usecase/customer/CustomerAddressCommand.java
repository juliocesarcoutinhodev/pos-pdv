package br.com.topone.backend.application.usecase.customer;

public record CustomerAddressCommand(
        String zipCode,
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state
) {
}
