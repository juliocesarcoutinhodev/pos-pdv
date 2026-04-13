package br.com.topone.backend.domain.exception;

public class LabelReportGenerationException extends RuntimeException {

    public LabelReportGenerationException(String message) {
        super(message);
    }

    public LabelReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
