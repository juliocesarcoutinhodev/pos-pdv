package br.com.topone.backend.domain.exception;

public class CustomerTaxIdAlreadyExistsException extends RuntimeException {
    public CustomerTaxIdAlreadyExistsException(String taxId) {
        super("Já existe um cliente com o documento: " + taxId);
    }
}
