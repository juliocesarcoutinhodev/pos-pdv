package br.com.topone.backend.domain.exception;

public class ZipCodeNotFoundException extends RuntimeException {

    public ZipCodeNotFoundException() {
        super("ZIP code not found");
    }
}

