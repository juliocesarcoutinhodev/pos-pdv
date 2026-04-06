package br.com.topone.backend.application.usecase;

import java.util.UUID;

public record GetUserByIdCommand(UUID id) {
}
