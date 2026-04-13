package br.com.topone.backend.domain.exception;

public class ProductBarcodeAlreadyExistsException extends RuntimeException {

    public ProductBarcodeAlreadyExistsException(String barcode) {
        super("Código de barras já cadastrado: " + barcode);
    }
}
