package br.com.topone.backend.application.usecase.supplier;

import br.com.topone.backend.domain.exception.SupplierNotFoundException;
import br.com.topone.backend.domain.exception.SupplierTaxIdAlreadyExistsException;
import br.com.topone.backend.domain.model.Address;
import br.com.topone.backend.domain.model.Contact;
import br.com.topone.backend.domain.model.Supplier;
import br.com.topone.backend.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateSupplierPatchUseCase {

    private final SupplierRepository supplierRepository;

    @Transactional
    public UpdateSupplierResult execute(UpdateSupplierPatchCommand command) {
        var supplier = supplierRepository.findById(command.id())
                .orElseThrow(SupplierNotFoundException::new);

        if (command.name() != null) {
            supplier.changeName(command.name());
        }

        var normalizedTaxId = Supplier.normalizeTaxId(command.taxId());
        if (normalizedTaxId != null && !normalizedTaxId.equals(supplier.getTaxId())
                && supplierRepository.findByTaxIdExcludingId(normalizedTaxId, supplier.getId()).isPresent()) {
            throw new SupplierTaxIdAlreadyExistsException();
        }
        if (command.taxId() != null) {
            supplier.changeTaxId(command.taxId());
        }

        if (command.email() != null) {
            supplier.changeEmail(command.email());
        }

        if (command.phone() != null) {
            supplier.changePhone(command.phone());
        }

        if (command.address() != null) {
            supplier.changeAddress(mergeAddress(supplier.getAddress(), command.address()));
        }

        if (command.contacts() != null) {
            supplier.assignContacts(toContacts(command.contacts()));
        }

        if (command.active() != null) {
            if (command.active()) {
                supplier.reactivate();
            } else {
                supplier.deactivate();
            }
        }

        supplier.touch();
        var saved = supplierRepository.save(supplier);
        log.info("Supplier patched | id={}", saved.getId());

        return toResult(saved);
    }

    private Address mergeAddress(Address current, SupplierAddressPatchCommand patch) {
        if (current == null) {
            return Address.create(
                    patch.zipCode(),
                    patch.street(),
                    patch.number(),
                    patch.complement(),
                    patch.district(),
                    patch.city(),
                    patch.state()
            );
        }

        return Address.create(
                patch.zipCode() != null ? patch.zipCode() : current.getZipCode(),
                patch.street() != null ? patch.street() : current.getStreet(),
                patch.number() != null ? patch.number() : current.getNumber(),
                patch.complement() != null ? patch.complement() : current.getComplement(),
                patch.district() != null ? patch.district() : current.getDistrict(),
                patch.city() != null ? patch.city() : current.getCity(),
                patch.state() != null ? patch.state() : current.getState()
        );
    }

    private UpdateSupplierResult toResult(Supplier supplier) {
        return new UpdateSupplierResult(
                supplier.getId(),
                supplier.getName(),
                supplier.getTaxId(),
                supplier.getEmail(),
                supplier.getPhone(),
                toAddressResult(supplier.getAddress()),
                toContactResults(supplier.getContacts()),
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

    private List<Contact> toContacts(List<SupplierContactCommand> contacts) {
        if (contacts == null) {
            return List.of();
        }
        return contacts.stream()
                .filter(Objects::nonNull)
                .map(this::toContact)
                .toList();
    }

    private Contact toContact(SupplierContactCommand contact) {
        if (contact == null) {
            return null;
        }
        return Contact.create(contact.name(), contact.email(), contact.phone());
    }

    private List<SupplierContactResult> toContactResults(List<Contact> contacts) {
        if (contacts == null) {
            return List.of();
        }
        return contacts.stream()
                .map(contact -> new SupplierContactResult(
                        contact.getId(),
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone()
                ))
                .toList();
    }
}
