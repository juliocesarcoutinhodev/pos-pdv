package br.com.topone.backend.domain.repository;

import java.time.LocalDate;

public record ProductFilter(
        String name,
        String sku,
        String barcode,
        String category,
        Boolean active,
        LocalDate createdDate
) {
}
