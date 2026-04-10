package br.com.topone.backend.application.usecase.supplier;

import br.com.topone.backend.domain.exception.SupplierNotFoundException;
import br.com.topone.backend.domain.model.Address;
import br.com.topone.backend.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetSupplierByIdUseCase {

    private final SupplierRepository supplierRepository;

    public GetSupplierByIdResult execute(GetSupplierByIdCommand command) {
        var supplier = supplierRepository.findById(command.id())
                .orElseThrow(SupplierNotFoundException::new);

        return new GetSupplierByIdResult(
                supplier.getId(),
                supplier.getName(),
                supplier.getTaxId(),
                supplier.getEmail(),
                supplier.getPhone(),
                toAddressResult(supplier.getAddress()),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt(),
                supplier.isActive()
        );
    }

    private SupplierAddressResult toAddressResult(Address address) {
        if (address == null) {
            return null;
        }

        return new SupplierAddressResult(
                address.getId(),
                address.getZipCode(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getDistrict(),
                address.getCity(),
                address.getState()
        );
    }
}
