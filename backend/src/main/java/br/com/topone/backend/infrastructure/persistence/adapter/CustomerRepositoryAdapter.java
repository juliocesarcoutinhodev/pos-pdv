package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.exception.CustomerNotFoundException;
import br.com.topone.backend.domain.model.Customer;
import br.com.topone.backend.domain.repository.CustomerFilter;
import br.com.topone.backend.domain.repository.CustomerRepository;
import br.com.topone.backend.domain.repository.PageResult;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.SortDirection;
import br.com.topone.backend.infrastructure.persistence.entity.CustomerEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.CustomerJpaRepository;
import br.com.topone.backend.infrastructure.persistence.mapper.CustomerMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository customerJpaRepository;
    private final CustomerMapper mapper;

    public CustomerRepositoryAdapter(CustomerJpaRepository customerJpaRepository, CustomerMapper mapper) {
        this.customerJpaRepository = customerJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity;
        if (customer.getId() != null) {
            entity = customerJpaRepository.findById(customer.getId())
                    .orElseThrow(() -> new CustomerNotFoundException(customer.getId()));
            mapper.updateEntity(customer, entity);
            entity.setAddress(mapper.toEntity(customer.getAddress()));
        } else {
            entity = mapper.toEntity(customer);
        }

        var saved = customerJpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return customerJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByTaxIdExcludingId(String taxId, UUID excludeId) {
        return customerJpaRepository.findByTaxIdExcludingId(taxId, excludeId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return customerJpaRepository.existsByTaxId(taxId);
    }

    @Override
    public PageResult<Customer> findAll(CustomerFilter filter, int page, int size, PageSort sort) {
        var namePattern = filter.name() != null && !filter.name().isBlank()
                ? "%" + filter.name().toLowerCase() + "%" : null;
        var taxIdPattern = filter.taxId() != null && !filter.taxId().isBlank()
                ? "%" + filter.taxId().replaceAll("\\D", "") + "%" : null;
        var emailPattern = filter.email() != null && !filter.email().isBlank()
                ? "%" + filter.email().toLowerCase() + "%" : null;

        var sortSpec = sort != null && sort.isSorted()
                ? Sort.by(
                sort.direction() == SortDirection.DESC
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                sort.field())
                : Sort.unsorted();

        var springPage = PageRequest.of(page, size, sortSpec);
        var pageResult = customerJpaRepository.searchByFilter(
                namePattern,
                taxIdPattern,
                emailPattern,
                filter.active(),
                springPage
        );

        var customers = pageResult.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(
                customers,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }
}
