package br.com.topone.backend.domain.exception;

public class CashRegisterSessionAlreadyOpenException extends RuntimeException {

    public CashRegisterSessionAlreadyOpenException() {
        super("Já existe um caixa aberto para o operador.");
    }
}

