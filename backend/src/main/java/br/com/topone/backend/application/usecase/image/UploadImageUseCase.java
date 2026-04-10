package br.com.topone.backend.application.usecase.image;

import br.com.topone.backend.domain.exception.InvalidImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadImageUseCase {

    private static final String IMAGE_CONTENT_TYPE_PREFIX = "image/";

    private final ImageStorageGateway imageStorageGateway;

    public UploadImageResult execute(UploadImageCommand command) {
        if (command.content() == null || command.content().length == 0) {
            throw new InvalidImageException("O arquivo de imagem é obrigatório.");
        }

        if (command.contentType() == null || !command.contentType().startsWith(IMAGE_CONTENT_TYPE_PREFIX)) {
            throw new InvalidImageException("Apenas arquivos de imagem são permitidos.");
        }

        var metadata = imageStorageGateway.upload(command);
        log.info("Image uploaded | imageId={} | size={} bytes", metadata.imageId(), metadata.size());

        return new UploadImageResult(
                metadata.imageId(),
                metadata.fileName(),
                metadata.contentType(),
                metadata.size(),
                Instant.now()
        );
    }
}
