package br.com.topone.backend.application.usecase.image;

import java.time.Instant;

public record UploadImageResult(
        String imageId,
        String fileName,
        String contentType,
        long size,
        Instant uploadedAt
) {
}
