package br.com.topone.backend.domain.exception;

public class InvalidCashOperationException extends RuntimeException {

    public InvalidCashOperationException(String message) {
        super(message);
    }
}

