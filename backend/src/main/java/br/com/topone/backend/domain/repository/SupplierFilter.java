package br.com.topone.backend.domain.repository;

public record SupplierFilter(
        String name,
        String taxId,
        String email,
        Boolean active
) {
}
