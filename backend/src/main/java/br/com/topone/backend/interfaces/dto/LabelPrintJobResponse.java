package br.com.topone.backend.interfaces.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LabelPrintJobResponse(
        UUID id,
        LocalDate referenceDate,
        int totalProducts,
        int totalLabels,
        Instant createdAt,
        List<LabelPrintJobItemResponse> items
) {
}
