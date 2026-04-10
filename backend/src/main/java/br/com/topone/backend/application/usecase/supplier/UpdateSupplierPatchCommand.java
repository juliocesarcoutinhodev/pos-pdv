package br.com.topone.backend.application.usecase.supplier;

import java.util.UUID;

public record UpdateSupplierPatchCommand(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        SupplierAddressPatchCommand address,
        java.util.List<SupplierContactCommand> contacts,
        Boolean active
) {
}
