package br.com.topone.backend.interfaces.dto;

public record ValidationError(
        String field,
        String message
) {
}
