package br.com.topone.backend.domain.exception;

public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException(String imageId) {
        super("Imagem não encontrada: " + imageId);
    }
}
