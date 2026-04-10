package br.com.topone.backend.application.usecase.customer;

import br.com.topone.backend.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListCustomersUseCase {

    private final CustomerRepository customerRepository;

    public ListCustomersResult execute(ListCustomersCommand command) {
        var pageResult = customerRepository.findAll(
                command.filter(),
                command.page(),
                command.size(),
                command.sort()
        );

        log.debug("List customers | page={} | size={} | total={}",
                command.page(), command.size(), pageResult.totalElements());

        var summaries = pageResult.content().stream()
                .map(customer -> new ListCustomersResult.CustomerSummary(
                        customer.getId(),
                        customer.getName(),
                        customer.getTaxId(),
                        customer.getEmail(),
                        customer.getPhone(),
                        customer.getImageId(),
                        customer.getCreatedAt(),
                        customer.getUpdatedAt(),
                        customer.isActive()
                ))
                .toList();

        return new ListCustomersResult(
                summaries,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }
}
