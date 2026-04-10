package br.com.topone.backend.domain.exception;

public class InvalidZipCodeException extends RuntimeException {

    public InvalidZipCodeException() {
        super("Invalid ZIP code");
    }
}

