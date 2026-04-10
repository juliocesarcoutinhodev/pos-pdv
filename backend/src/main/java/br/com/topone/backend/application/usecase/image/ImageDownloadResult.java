package br.com.topone.backend.application.usecase.image;

public record ImageDownloadResult(
        String imageId,
        String fileName,
        String contentType,
        long size,
        byte[] content
) {
}
