package br.com.topone.backend.application.usecase.customer;

import br.com.topone.backend.domain.exception.CustomerNotFoundException;
import br.com.topone.backend.domain.exception.CustomerTaxIdAlreadyExistsException;
import br.com.topone.backend.domain.model.Address;
import br.com.topone.backend.domain.model.Customer;
import br.com.topone.backend.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateCustomerPatchUseCase {

    private final CustomerRepository customerRepository;

    @Transactional
    public UpdateCustomerResult execute(UpdateCustomerPatchCommand command) {
        var customer = customerRepository.findById(command.id())
                .orElseThrow(() -> new CustomerNotFoundException(command.id()));

        if (command.name() != null) {
            customer.changeName(command.name());
        }

        var normalizedTaxId = Customer.normalizeTaxId(command.taxId());
        if (normalizedTaxId != null && !normalizedTaxId.equals(customer.getTaxId())
                && customerRepository.findByTaxIdExcludingId(normalizedTaxId, customer.getId()).isPresent()) {
            throw new CustomerTaxIdAlreadyExistsException(normalizedTaxId);
        }
        if (command.taxId() != null) {
            customer.changeTaxId(command.taxId());
        }

        if (command.email() != null) {
            customer.changeEmail(command.email());
        }

        if (command.phone() != null) {
            customer.changePhone(command.phone());
        }

        if (command.imageId() != null) {
            customer.changeImageId(command.imageId());
        }

        if (command.address() != null) {
            customer.changeAddress(mergeAddress(customer.getAddress(), command.address()));
        }

        if (command.active() != null) {
            if (command.active()) {
                customer.reactivate();
            } else {
                customer.deactivate();
            }
        }

        customer.touch();
        var saved = customerRepository.save(customer);
        log.info("Customer patched | id={}", saved.getId());

        return toResult(saved);
    }

    private Address mergeAddress(Address current, CustomerAddressPatchCommand patch) {
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

    private UpdateCustomerResult toResult(Customer customer) {
        return new UpdateCustomerResult(
                customer.getId(),
                customer.getName(),
                customer.getTaxId(),
                customer.getEmail(),
                customer.getPhone(),
                toAddressResult(customer.getAddress()),
                customer.getImageId(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.isActive()
        );
    }

    private CustomerAddressResult toAddressResult(Address address) {
        if (address == null) {
            return null;
        }

        return new CustomerAddressResult(
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
