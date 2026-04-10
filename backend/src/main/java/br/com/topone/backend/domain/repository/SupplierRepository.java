package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.Supplier;

import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository {
    Supplier save(Supplier supplier);

    Optional<Supplier> findById(UUID id);

    Optional<Supplier> findByTaxId(String taxId);

    boolean existsByTaxId(String taxId);

    Optional<Supplier> findByTaxIdExcludingId(String taxId, UUID excludeId);

    PageResult<Supplier> findAll(SupplierFilter filter, int page, int size, PageSort sort);
}
