package br.com.topone.backend.domain.exception;

public class CashRegisterSessionNotFoundException extends RuntimeException {

    public CashRegisterSessionNotFoundException() {
        super("Caixa não encontrado.");
    }
}
