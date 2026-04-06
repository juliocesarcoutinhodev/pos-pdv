package br.com.topone.backend.interfaces.dto;

import java.util.List;

public record ValidationErrorResponse(
        int status,
        String error,
        List<ValidationError> details,
        String timestamp
) {
}
