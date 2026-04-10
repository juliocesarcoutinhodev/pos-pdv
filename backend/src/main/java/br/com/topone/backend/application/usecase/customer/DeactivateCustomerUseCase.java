package br.com.topone.backend.application.usecase.customer;

import br.com.topone.backend.domain.exception.CustomerNotFoundException;
import br.com.topone.backend.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeactivateCustomerUseCase {

    private final CustomerRepository customerRepository;

    @Transactional
    public DeactivateCustomerResult execute(DeactivateCustomerCommand command) {
        var customer = customerRepository.findById(command.id())
                .orElseThrow(() -> new CustomerNotFoundException(command.id()));

        customer.deactivate();
        customer.touch();

        var saved = customerRepository.save(customer);
        log.info("Customer deactivated | id={}", saved.getId());

        return new DeactivateCustomerResult(saved.getId(), saved.isActive());
    }
}
