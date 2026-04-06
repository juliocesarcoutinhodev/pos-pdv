package br.com.topone.backend.application.usecase.user;

import java.util.UUID;

public record DeactivateUserCommand(UUID id) {
}
