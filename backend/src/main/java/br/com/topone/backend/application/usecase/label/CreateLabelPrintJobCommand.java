package br.com.topone.backend.application.usecase.label;

import java.time.LocalDate;
import java.util.List;

public record CreateLabelPrintJobCommand(
        LocalDate referenceDate,
        List<CreateLabelPrintJobItemCommand> items
) {
}
