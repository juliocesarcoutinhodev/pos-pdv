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
public class UpdateSupplierUseCase {

    private final SupplierRepository supplierRepository;

    @Transactional
    public UpdateSupplierResult execute(UpdateSupplierCommand command) {
        var supplier = supplierRepository.findById(command.id())
                .orElseThrow(SupplierNotFoundException::new);

        var normalizedTaxId = Supplier.normalizeTaxId(command.taxId());
        if (normalizedTaxId != null && !normalizedTaxId.equals(supplier.getTaxId())
                && supplierRepository.findByTaxIdExcludingId(normalizedTaxId, supplier.getId()).isPresent()) {
            throw new SupplierTaxIdAlreadyExistsException();
        }

        if (command.name() != null) {
            supplier.changeName(command.name());
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
            supplier.changeAddress(toAddress(command.address()));
        }

        if (command.contacts() != null) {
            supplier.assignContacts(toContacts(command.contacts()));
        }

        supplier.touch();
        var saved = supplierRepository.save(supplier);
        log.info("Supplier updated | id={}", saved.getId());

        return toResult(saved);
    }

    private Address toAddress(SupplierAddressCommand address) {
        if (address == null) {
            return null;
        }

        return Address.create(
                address.zipCode(),
                address.street(),
                address.number(),
                address.complement(),
                address.district(),
                address.city(),
                address.state()
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
