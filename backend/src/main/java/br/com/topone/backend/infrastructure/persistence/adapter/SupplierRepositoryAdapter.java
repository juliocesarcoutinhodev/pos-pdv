package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.exception.SupplierNotFoundException;
import br.com.topone.backend.domain.model.Supplier;
import br.com.topone.backend.domain.repository.PageResult;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.SortDirection;
import br.com.topone.backend.domain.repository.SupplierFilter;
import br.com.topone.backend.domain.repository.SupplierRepository;
import br.com.topone.backend.infrastructure.persistence.entity.SupplierEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.SupplierJpaRepository;
import br.com.topone.backend.infrastructure.persistence.mapper.SupplierMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SupplierRepositoryAdapter implements SupplierRepository {

    private final SupplierJpaRepository supplierJpaRepository;
    private final SupplierMapper mapper;

    public SupplierRepositoryAdapter(SupplierJpaRepository supplierJpaRepository, SupplierMapper mapper) {
        this.supplierJpaRepository = supplierJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Supplier save(Supplier supplier) {
        SupplierEntity entity;
        if (supplier.getId() != null) {
            entity = supplierJpaRepository.findById(supplier.getId())
                    .orElseThrow(SupplierNotFoundException::new);
            mapper.updateEntity(supplier, entity);
            entity.setAddress(mapper.toEntity(supplier.getAddress()));
            if (entity.getContacts() == null) {
                entity.setContacts(new ArrayList<>());
            } else {
                entity.getContacts().clear();
            }

            var contactEntities = mapper.toContactEntityList(supplier.getContacts());
            if (contactEntities != null) {
                entity.getContacts().addAll(contactEntities);
            }
        } else {
            entity = mapper.toEntity(supplier);
            if (entity.getContacts() == null) {
                entity.setContacts(new ArrayList<>());
            }
        }

        var saved = supplierJpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Supplier> findById(UUID id) {
        return supplierJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Supplier> findByTaxId(String taxId) {
        return supplierJpaRepository.findByTaxId(taxId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByTaxId(String taxId) {
        return supplierJpaRepository.existsByTaxId(taxId);
    }

    @Override
    public Optional<Supplier> findByTaxIdExcludingId(String taxId, UUID excludeId) {
        return supplierJpaRepository.findByTaxIdExcludingId(taxId, excludeId).map(mapper::toDomain);
    }

    @Override
    public PageResult<Supplier> findAll(SupplierFilter filter, int page, int size, PageSort sort) {
        var namePattern = filter.name() != null && !filter.name().isBlank()
                ? "%" + filter.name().toLowerCase() + "%" : null;
        var normalizedNamePattern = normalizeSupplierNamePattern(filter.name());
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
        var pageResult = supplierJpaRepository.searchByFilter(
                namePattern,
                normalizedNamePattern,
                taxIdPattern,
                emailPattern,
                filter.active(),
                springPage
        );

        var suppliers = pageResult.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(
                suppliers,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    private String normalizeSupplierNamePattern(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        var normalized = name
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\\s._/\\-]", "");

        return normalized.isBlank() ? null : "%" + normalized + "%";
    }
}
