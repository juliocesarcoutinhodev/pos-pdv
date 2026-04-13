package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.repository.PageSort;

import java.time.LocalDate;

public record ListLabelPrintJobsCommand(
        LocalDate referenceDate,
        int page,
        int size,
        PageSort sort
) {
}
