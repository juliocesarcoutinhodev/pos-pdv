package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank @Size(max = 120) String name,
        @Email @Size(max = 255) String email,
        @Size(max = 30) String phone
) {
}
