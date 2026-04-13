package br.com.topone.backend.domain.repository;

public record ProductFilter(
        String name,
        String sku,
        String barcode,
        String category,
        Boolean active
) {
}
