package br.com.topone.backend.application.usecase.supplier;

public record CreateSupplierCommand(
        String name,
        String taxId,
        String email,
        String phone,
        SupplierAddressCommand address,
        java.util.List<SupplierContactCommand> contacts
) {
}
