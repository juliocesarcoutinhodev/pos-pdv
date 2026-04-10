package br.com.topone.backend.domain.exception;

public class InvalidCnpjException extends RuntimeException {

    public InvalidCnpjException() {
        super("Invalid CNPJ");
    }
}

