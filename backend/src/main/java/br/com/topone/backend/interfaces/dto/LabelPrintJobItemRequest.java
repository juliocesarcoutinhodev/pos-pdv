package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LabelPrintJobItemRequest(
        @NotNull UUID productId,
        @NotNull @Min(1) Integer quantity
) {
}
