package br.com.topone.backend.interfaces.dto;

public record ConflictResponse(
        int status,
        String error,
        String message,
        String timestamp
) {
}
