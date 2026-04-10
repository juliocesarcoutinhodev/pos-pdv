package br.com.topone.backend.application.usecase.image;

public interface ImageStorageGateway {

    ImageMetadataResult upload(UploadImageCommand command);

    ImageDownloadResult download(String imageId);

    ImageMetadataResult metadata(String imageId);
}
