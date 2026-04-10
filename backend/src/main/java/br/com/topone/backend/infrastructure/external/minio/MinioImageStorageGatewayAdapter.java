package br.com.topone.backend.infrastructure.external.minio;

import br.com.topone.backend.application.usecase.image.ImageDownloadResult;
import br.com.topone.backend.application.usecase.image.ImageMetadataResult;
import br.com.topone.backend.application.usecase.image.ImageStorageGateway;
import br.com.topone.backend.application.usecase.image.UploadImageCommand;
import br.com.topone.backend.domain.exception.ImageNotFoundException;
import br.com.topone.backend.domain.exception.ImageStorageIntegrationException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MinioImageStorageGatewayAdapter implements ImageStorageGateway {

    private static final String NO_SUCH_KEY = "NoSuchKey";
    private static final String NO_SUCH_BUCKET = "NoSuchBucket";
    private static final String ORIGINAL_FILE_NAME_KEY = "original-filename";

    private final MinioClient minioClient;
    private final MinioProperties properties;
    private volatile boolean bucketVerified;

    @Autowired
    public MinioImageStorageGatewayAdapter(MinioProperties properties) {
        this(createClient(properties), properties);
    }

    MinioImageStorageGatewayAdapter(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
        this.bucketVerified = false;
    }

    @Override
    public ImageMetadataResult upload(UploadImageCommand command) {
        var imageId = UUID.randomUUID().toString();
        var fileName = normalizeFileName(command.originalFilename(), imageId);

        try {
            ensureBucketIfNeeded();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(imageId)
                    .stream(new ByteArrayInputStream(command.content()), command.content().length, -1)
                    .contentType(command.contentType())
                    .userMetadata(Map.of(ORIGINAL_FILE_NAME_KEY, fileName))
                    .build());

            return new ImageMetadataResult(
                    imageId,
                    fileName,
                    command.contentType(),
                    command.content().length,
                    Instant.now()
            );
        } catch (Exception ex) {
            log.error("Failed to upload image to MinIO", ex);
            throw new ImageStorageIntegrationException("Falha ao enviar imagem para o storage.", ex);
        }
    }

    @Override
    public ImageDownloadResult download(String imageId) {
        try {
            var stat = stat(imageId);
            try (var stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(imageId)
                    .build())) {
                var content = stream.readAllBytes();
                var fileName = extractFileName(stat.userMetadata(), imageId);
                var contentType = normalizeContentType(stat.contentType());

                return new ImageDownloadResult(
                        imageId,
                        fileName,
                        contentType,
                        stat.size(),
                        content
                );
            }
        } catch (ImageNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to download image from MinIO | imageId={}", imageId, ex);
            throw new ImageStorageIntegrationException("Falha ao baixar imagem do storage.", ex);
        }
    }

    @Override
    public ImageMetadataResult metadata(String imageId) {
        try {
            var stat = stat(imageId);
            return new ImageMetadataResult(
                    imageId,
                    extractFileName(stat.userMetadata(), imageId),
                    normalizeContentType(stat.contentType()),
                    stat.size(),
                    stat.lastModified() != null ? stat.lastModified().toInstant() : null
            );
        } catch (ImageNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to fetch image metadata from MinIO | imageId={}", imageId, ex);
            throw new ImageStorageIntegrationException("Falha ao buscar metadados da imagem.", ex);
        }
    }

    private io.minio.StatObjectResponse stat(String imageId) throws Exception {
        try {
            return minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(imageId)
                    .build());
        } catch (ErrorResponseException ex) {
            if (NO_SUCH_KEY.equalsIgnoreCase(ex.errorResponse().code())) {
                throw new ImageNotFoundException(imageId);
            }
            if (NO_SUCH_BUCKET.equalsIgnoreCase(ex.errorResponse().code())) {
                throw new ImageStorageIntegrationException("Bucket de imagens não encontrado.", ex);
            }
            throw ex;
        }
    }

    private synchronized void ensureBucketIfNeeded() {
        if (bucketVerified) {
            return;
        }
        if (!properties.isAutoCreateBucket()) {
            bucketVerified = true;
            return;
        }
        try {
            var exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(properties.getBucket())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucket())
                        .build());
            }
            bucketVerified = true;
        } catch (Exception ex) {
            throw new ImageStorageIntegrationException("Falha ao validar bucket de imagens no storage.", ex);
        }
    }

    private static MinioClient createClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    private String extractFileName(Map<String, String> userMetadata, String imageId) {
        if (userMetadata == null || userMetadata.isEmpty()) {
            return imageId;
        }

        var fileName = userMetadata.get(ORIGINAL_FILE_NAME_KEY);
        if (fileName == null || fileName.isBlank()) {
            return imageId;
        }
        return fileName;
    }

    private String normalizeFileName(String fileName, String imageId) {
        if (fileName == null) {
            return imageId;
        }

        var trimmed = fileName.trim();
        return trimmed.isEmpty() ? imageId : trimmed;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType;
    }
}
