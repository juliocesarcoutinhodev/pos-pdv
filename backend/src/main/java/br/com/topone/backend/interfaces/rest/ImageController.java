package br.com.topone.backend.interfaces.rest;

import br.com.topone.backend.application.usecase.image.DownloadImageUseCase;
import br.com.topone.backend.application.usecase.image.GetImageMetadataUseCase;
import br.com.topone.backend.application.usecase.image.UploadImageCommand;
import br.com.topone.backend.application.usecase.image.UploadImageUseCase;
import br.com.topone.backend.domain.exception.ImageStorageIntegrationException;
import br.com.topone.backend.infrastructure.security.AuthorizationPolicies;
import br.com.topone.backend.interfaces.dto.ImageMetadataResponse;
import br.com.topone.backend.interfaces.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {

    private final UploadImageUseCase uploadImageUseCase;
    private final DownloadImageUseCase downloadImageUseCase;
    private final GetImageMetadataUseCase getImageMetadataUseCase;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<ImageUploadResponse> upload(@RequestPart("file") MultipartFile file) {
        try {
            var result = uploadImageUseCase.execute(new UploadImageCommand(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes()
            ));

            return ResponseEntity.status(HttpStatus.CREATED).body(new ImageUploadResponse(
                    result.imageId(),
                    result.fileName(),
                    result.contentType(),
                    result.size(),
                    result.uploadedAt(),
                    buildDownloadUrl(result.imageId()),
                    buildMetadataUrl(result.imageId())
            ));
        } catch (IOException ex) {
            throw new ImageStorageIntegrationException("Falha ao processar upload da imagem.", ex);
        }
    }

    @GetMapping("/{imageId}")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<byte[]> download(@PathVariable String imageId) {
        var result = downloadImageUseCase.execute(imageId);
        var mediaType = parseMediaType(result.contentType());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(result.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .contentType(mediaType)
                .contentLength(result.size())
                .body(result.content());
    }

    @GetMapping("/{imageId}/metadata")
    @PreAuthorize(AuthorizationPolicies.AUTHENTICATED)
    public ResponseEntity<ImageMetadataResponse> metadata(@PathVariable String imageId) {
        var metadata = getImageMetadataUseCase.execute(imageId);
        return ResponseEntity.ok(new ImageMetadataResponse(
                metadata.imageId(),
                metadata.fileName(),
                metadata.contentType(),
                metadata.size(),
                metadata.lastModified(),
                buildDownloadUrl(metadata.imageId())
        ));
    }

    private String buildDownloadUrl(String imageId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/images/")
                .path(imageId)
                .toUriString();
    }

    private String buildMetadataUrl(String imageId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/images/")
                .path(imageId)
                .path("/metadata")
                .toUriString();
    }

    private MediaType parseMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMimeTypeException ex) {
            return MediaType.parseMediaType(MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        }
    }
}
