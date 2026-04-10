package br.com.topone.backend.domain.exception;

public class ImageStorageIntegrationException extends RuntimeException {
    public ImageStorageIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageStorageIntegrationException(String message) {
        super(message);
    }
}
