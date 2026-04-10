package br.com.topone.backend.application.usecase.image;

import br.com.topone.backend.domain.exception.InvalidImageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadImageUseCaseTest {

    @Mock
    private ImageStorageGateway imageStorageGateway;

    @InjectMocks
    private UploadImageUseCase useCase;

    @Test
    void shouldUploadValidImage() {
        var command = new UploadImageCommand("avatar.png", "image/png", new byte[]{1, 2, 3});
        when(imageStorageGateway.upload(any())).thenReturn(new ImageMetadataResult(
                "img-1",
                "avatar.png",
                "image/png",
                3,
                Instant.now()
        ));

        var result = useCase.execute(command);

        assertThat(result.imageId()).isEqualTo("img-1");
        assertThat(result.contentType()).isEqualTo("image/png");
    }

    @Test
    void shouldRejectNonImageContentType() {
        var command = new UploadImageCommand("arquivo.pdf", "application/pdf", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidImageException.class);
    }
}
