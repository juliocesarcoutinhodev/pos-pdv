package br.com.topone.backend.domain.exception;

import java.util.UUID;

public class InsufficientProductStockException extends RuntimeException {

    public InsufficientProductStockException(UUID productId) {
        super("Estoque insuficiente para o produto: " + productId);
    }
}

