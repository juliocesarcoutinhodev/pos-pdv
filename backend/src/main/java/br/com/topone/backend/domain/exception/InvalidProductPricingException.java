package br.com.topone.backend.domain.exception;

public class InvalidProductPricingException extends RuntimeException {

    public InvalidProductPricingException(String message) {
        super(message);
    }
}
