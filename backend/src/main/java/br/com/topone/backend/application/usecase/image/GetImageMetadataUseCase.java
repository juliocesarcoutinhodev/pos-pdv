package br.com.topone.backend.application.usecase.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetImageMetadataUseCase {

    private final ImageStorageGateway imageStorageGateway;

    public ImageMetadataResult execute(String imageId) {
        var metadata = imageStorageGateway.metadata(imageId);
        log.debug("Image metadata loaded | imageId={}", metadata.imageId());
        return metadata;
    }
}
