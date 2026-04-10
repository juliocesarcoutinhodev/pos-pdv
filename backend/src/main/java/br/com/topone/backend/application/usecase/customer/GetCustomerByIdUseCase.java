package br.com.topone.backend.application.usecase.customer;

import br.com.topone.backend.domain.exception.CustomerNotFoundException;
import br.com.topone.backend.domain.model.Address;
import br.com.topone.backend.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetCustomerByIdUseCase {

    private final CustomerRepository customerRepository;

    public GetCustomerByIdResult execute(GetCustomerByIdCommand command) {
        var customer = customerRepository.findById(command.id())
                .orElseThrow(() -> new CustomerNotFoundException(command.id()));

        log.debug("Get customer by id | id={}", customer.getId());

        return new GetCustomerByIdResult(
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
