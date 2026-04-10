package br.com.topone.backend.application.usecase.supplier;

public record SupplierContactCommand(
        String name,
        String email,
        String phone
) {
}
