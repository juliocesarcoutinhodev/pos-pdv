package br.com.topone.backend.domain.exception;

public class CustomerReportGenerationException extends RuntimeException {

    public CustomerReportGenerationException(String message) {
        super(message);
    }

    public CustomerReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
