package br.com.topone.backend.interfaces.dto;

import java.time.Instant;

public record ImageUploadResponse(
        String imageId,
        String fileName,
        String contentType,
        long size,
        Instant uploadedAt,
        String downloadUrl,
        String metadataUrl
) {
}
