package br.com.topone.backend.application.usecase.user;

import java.util.UUID;

public record GetUserByIdCommand(UUID id) {
}
