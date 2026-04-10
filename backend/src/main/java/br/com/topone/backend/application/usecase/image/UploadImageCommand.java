package br.com.topone.backend.application.usecase.image;

public record UploadImageCommand(
        String originalFilename,
        String contentType,
        byte[] content
) {
}
