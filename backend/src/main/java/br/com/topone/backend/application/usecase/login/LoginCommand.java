package br.com.topone.backend.application.usecase.login;

public record LoginCommand(
        String email,
        String password
) {
}
