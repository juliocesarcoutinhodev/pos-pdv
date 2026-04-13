package br.com.topone.backend.application.usecase.label;

import java.util.UUID;

public record GetLabelPrintJobByIdCommand(
        UUID id
) {
}
