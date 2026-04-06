package br.com.topone.backend.interfaces.dto;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String timestamp
) {
}
