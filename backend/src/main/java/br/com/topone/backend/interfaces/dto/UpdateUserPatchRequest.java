package br.com.topone.backend.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record UpdateUserPatchRequest(
        @Email String email,
        @Size(max = 100) String name,
        @Size(min = 6) String password,
        Set<UUID> roleIds
) {
}
