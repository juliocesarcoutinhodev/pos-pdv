package br.com.topone.backend.application.usecase;

public record RegisterUserCommand(
        String email,
        String name,
        String password
) {
}
