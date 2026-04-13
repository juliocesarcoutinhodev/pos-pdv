package br.com.topone.backend.domain.exception;

public class ProductSkuAlreadyExistsException extends RuntimeException {

    public ProductSkuAlreadyExistsException(String sku) {
        super("SKU do produto já cadastrado: " + sku);
    }
}
