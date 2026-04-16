package br.com.topone.backend.domain.exception;

public class InvalidSaleException extends RuntimeException {

    public InvalidSaleException(String message) {
        super(message);
    }
}

