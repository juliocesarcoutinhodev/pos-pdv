package br.com.topone.backend.application.usecase.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadImageUseCase {

    private final ImageStorageGateway imageStorageGateway;

    public ImageDownloadResult execute(String imageId) {
        var image = imageStorageGateway.download(imageId);
        log.debug("Image downloaded | imageId={} | size={} bytes", image.imageId(), image.size());
        return image;
    }
}
