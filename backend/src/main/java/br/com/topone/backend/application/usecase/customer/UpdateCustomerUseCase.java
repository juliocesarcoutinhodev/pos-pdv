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
public class UpdateCustomerUseCase {

    private final CustomerRepository customerRepository;

    @Transactional
    public UpdateCustomerResult execute(UpdateCustomerCommand command) {
        var customer = customerRepository.findById(command.id())
                .orElseThrow(() -> new CustomerNotFoundException(command.id()));

        var normalizedTaxId = Customer.normalizeTaxId(command.taxId());
        if (!normalizedTaxId.equals(customer.getTaxId())
                && customerRepository.findByTaxIdExcludingId(normalizedTaxId, customer.getId()).isPresent()) {
            throw new CustomerTaxIdAlreadyExistsException(normalizedTaxId);
        }

        customer.changeName(command.name());
        customer.changeTaxId(command.taxId());
        customer.changeEmail(command.email());
        customer.changePhone(command.phone());
        customer.changeBirthDate(command.birthDate());
        customer.changeGender(command.gender());
        customer.changeIeOrRg(command.ieOrRg());
        customer.changeImageId(command.imageId());
        customer.changeAddress(toAddress(command.address()));
        customer.touch();

        var saved = customerRepository.save(customer);
        log.info("Customer updated | id={}", saved.getId());

        return toResult(saved);
    }

    private Address toAddress(CustomerAddressCommand address) {
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

    private UpdateCustomerResult toResult(Customer customer) {
        return new UpdateCustomerResult(
                customer.getId(),
                customer.getName(),
                customer.getTaxId(),
                customer.getEmail(),
                customer.getPhone(),
                toAddressResult(customer.getAddress()),
                customer.getBirthDate(),
                customer.getGender(),
                customer.getIeOrRg(),
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
