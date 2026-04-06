package br.com.topone.backend.application.usecase;

public record LoginCommand(
        String email,
        String password
) {
}
