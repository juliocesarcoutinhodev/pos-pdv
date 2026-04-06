package br.com.topone.backend.domain.exception;

public class RefreshTokenRevokedException extends RuntimeException {
    public RefreshTokenRevokedException() {
        super("Refresh token has been revoked");
    }
}
