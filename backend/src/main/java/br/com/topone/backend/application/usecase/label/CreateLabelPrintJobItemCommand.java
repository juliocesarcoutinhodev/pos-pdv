package br.com.topone.backend.application.usecase.label;

import java.util.UUID;

public record CreateLabelPrintJobItemCommand(
        UUID productId,
        Integer quantity
) {
}
