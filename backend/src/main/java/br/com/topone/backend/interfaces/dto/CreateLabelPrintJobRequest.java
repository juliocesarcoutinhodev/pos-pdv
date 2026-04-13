package br.com.topone.backend.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record CreateLabelPrintJobRequest(
        LocalDate referenceDate,
        @NotEmpty List<@Valid LabelPrintJobItemRequest> items
) {
}
