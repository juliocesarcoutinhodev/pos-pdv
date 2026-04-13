package br.com.topone.backend.application.usecase.label;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ListLabelPrintJobsResult(
        List<JobSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public record JobSummary(
            UUID id,
            LocalDate referenceDate,
            int totalProducts,
            int totalLabels,
            Instant createdAt
    ) {
    }
}
