package br.com.topone.backend.domain.repository;

public record CustomerFilter(
        String name,
        String taxId,
        String email,
        Boolean active
) {
}
