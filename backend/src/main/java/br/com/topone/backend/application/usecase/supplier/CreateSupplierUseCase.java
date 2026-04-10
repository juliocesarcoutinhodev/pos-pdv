package br.com.topone.backend.application.usecase.supplier;

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
public class CreateSupplierUseCase {

    private final SupplierRepository supplierRepository;

    @Transactional
    public CreateSupplierResult execute(CreateSupplierCommand command) {
        var supplier = Supplier.create(
                command.name(),
                command.taxId(),
                command.email(),
                command.phone(),
                toAddress(command.address()),
                toContacts(command.contacts())
        );

        if (supplierRepository.existsByTaxId(supplier.getTaxId())) {
            throw new SupplierTaxIdAlreadyExistsException();
        }

        var saved = supplierRepository.save(supplier);
        log.info("Supplier created | taxId={} | id={}", saved.getTaxId(), saved.getId());

        return new CreateSupplierResult(
                saved.getId(),
                saved.getName(),
                saved.getTaxId(),
                saved.getEmail(),
                saved.getPhone(),
                toAddressResult(saved.getAddress()),
                toContactResults(saved.getContacts()),
                saved.getCreatedAt()
        );
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
