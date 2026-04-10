package br.com.topone.backend.application.usecase.customer;

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
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;

    @Transactional
    public CreateCustomerResult execute(CreateCustomerCommand command) {
        var customer = Customer.create(
                command.name(),
                command.taxId(),
                command.email(),
                command.phone(),
                command.imageId(),
                toAddress(command.address())
        );

        if (customerRepository.existsByTaxId(customer.getTaxId())) {
            throw new CustomerTaxIdAlreadyExistsException(customer.getTaxId());
        }

        var saved = customerRepository.save(customer);
        log.info("Customer created | taxId={} | id={}", saved.getTaxId(), saved.getId());

        return new CreateCustomerResult(
                saved.getId(),
                saved.getName(),
                saved.getTaxId(),
                saved.getEmail(),
                saved.getPhone(),
                toAddressResult(saved.getAddress()),
                saved.getImageId(),
                saved.getCreatedAt()
        );
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
