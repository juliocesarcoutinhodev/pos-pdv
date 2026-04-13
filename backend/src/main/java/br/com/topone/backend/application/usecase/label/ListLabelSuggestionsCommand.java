package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.repository.PageSort;

import java.time.LocalDate;

public record ListLabelSuggestionsCommand(
        LocalDate date,
        String name,
        String sku,
        String category,
        int page,
        int size,
        PageSort sort
) {
}
