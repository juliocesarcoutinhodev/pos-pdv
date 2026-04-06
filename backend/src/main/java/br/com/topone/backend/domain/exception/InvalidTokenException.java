package br.com.topone.backend.domain.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Invalid refresh token");
    }
}
