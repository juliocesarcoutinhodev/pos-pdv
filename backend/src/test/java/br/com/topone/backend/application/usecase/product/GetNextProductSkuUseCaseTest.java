package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNextProductSkuUseCaseTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetNextProductSkuUseCase useCase;

    @Test
    void shouldReturnRandomSixDigitsSkuWhenCandidateIsAvailable() {
        when(productRepository.existsBySku(anyString())).thenReturn(false);

        var result = useCase.execute();

        assertThat(result).matches("\\d{6}");
        verify(productRepository).existsBySku(result);
    }

    @Test
    void shouldTryAgainWhenGeneratedSkuAlreadyExists() {
        when(productRepository.existsBySku(anyString())).thenReturn(true, false);

        var result = useCase.execute();

        assertThat(result).matches("\\d{6}");
        verify(productRepository, times(2)).existsBySku(anyString());
    }
}
