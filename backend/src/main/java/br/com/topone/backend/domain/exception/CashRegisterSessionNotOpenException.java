package br.com.topone.backend.domain.exception;

public class CashRegisterSessionNotOpenException extends RuntimeException {

    public CashRegisterSessionNotOpenException() {
        super("Não existe caixa aberto para o operador.");
    }
}

