package br.com.topone.backend.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LogoutRequest(
        @NotBlank(message = "Refresh token é obrigatório")
        String refreshToken,
        Boolean revokeAll
) {
}
