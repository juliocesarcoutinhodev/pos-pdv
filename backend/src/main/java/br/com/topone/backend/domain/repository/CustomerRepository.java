package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.Customer;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByTaxIdExcludingId(String taxId, UUID id);

    boolean existsByTaxId(String taxId);

    PageResult<Customer> findAll(CustomerFilter filter, int page, int size, PageSort sort);
}
