package br.com.topone.backend.domain.exception;

public class CnpjNotFoundException extends RuntimeException {

    public CnpjNotFoundException() {
        super("CNPJ not found");
    }
}

