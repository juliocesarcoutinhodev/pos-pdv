package br.com.topone.backend.domain.exception;

public class InvalidLabelPrintJobException extends RuntimeException {

    public InvalidLabelPrintJobException(String message) {
        super(message);
    }
}
