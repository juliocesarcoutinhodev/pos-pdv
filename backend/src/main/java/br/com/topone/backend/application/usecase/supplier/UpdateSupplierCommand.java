package br.com.topone.backend.application.usecase.supplier;

import java.util.UUID;

public record UpdateSupplierCommand(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        SupplierAddressCommand address
) {
}
