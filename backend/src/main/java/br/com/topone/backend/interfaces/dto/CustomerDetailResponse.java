package br.com.topone.backend.interfaces.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CustomerDetailResponse(
        UUID id,
        String name,
        String taxId,
        String email,
        String phone,
        AddressResponse address,
        LocalDate birthDate,
        String gender,
        String ieOrRg,
        String imageId,
        Instant createdAt,
        Instant updatedAt,
        boolean active
) {
}
