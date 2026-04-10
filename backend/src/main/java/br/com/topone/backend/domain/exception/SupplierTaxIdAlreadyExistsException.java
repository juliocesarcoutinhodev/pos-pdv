package br.com.topone.backend.domain.exception;

public class SupplierTaxIdAlreadyExistsException extends RuntimeException {

    public SupplierTaxIdAlreadyExistsException() {
        super("Supplier tax id already registered");
    }
}
