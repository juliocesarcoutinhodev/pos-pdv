package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.Size;

public record UpdateRolePatchRequest(
        @Size(max = 20) String name,
        @Size(max = 255) String description
) {
}
