package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(min = 6) String password,
        @NotEmpty Set<UUID> roleIds
) {
}
