package br.com.topone.backend.domain.exception;

public class SupplierNotFoundException extends RuntimeException {

    public SupplierNotFoundException() {
        super("Supplier not found");
    }
}
