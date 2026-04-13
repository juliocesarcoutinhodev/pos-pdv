package br.com.topone.backend.domain.exception;

import java.util.UUID;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException() {
        super("Produto não encontrado");
    }

    public ProductNotFoundException(UUID id) {
        super("Produto não encontrado: " + id);
    }
}
