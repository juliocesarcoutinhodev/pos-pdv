package br.com.topone.backend.domain.exception;

public class InvalidCustomerReportException extends RuntimeException {

    public InvalidCustomerReportException(String message) {
        super(message);
    }
}
