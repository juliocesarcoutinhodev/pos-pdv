package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.exception.LabelPrintJobNotFoundException;
import br.com.topone.backend.domain.model.LabelPrintJob;
import br.com.topone.backend.domain.repository.LabelPrintJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateLabelPrintJobReportUseCaseTest {

    @Mock
    private LabelPrintJobRepository labelPrintJobRepository;

    @Mock
    private LabelReportGateway labelReportGateway;

    @InjectMocks
    private GenerateLabelPrintJobReportUseCase useCase;

    @Test
    void shouldGenerateReportWhenJobExists() {
        var jobId = UUID.randomUUID();
        var job = LabelPrintJob.builder()
                .id(jobId)
                .referenceDate(LocalDate.now())
                .build();

        when(labelPrintJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(labelReportGateway.generateJobReportPdf(jobId.toString())).thenReturn(new byte[]{1, 2, 3});

        var result = useCase.execute(new GenerateLabelPrintJobReportCommand(jobId));

        assertThat(result.fileName()).isEqualTo("etiquetas-" + jobId + ".pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.content()).containsExactly((byte) 1, (byte) 2, (byte) 3);
        verify(labelReportGateway).generateJobReportPdf(jobId.toString());
    }

    @Test
    void shouldThrowWhenJobDoesNotExist() {
        var jobId = UUID.randomUUID();
        when(labelPrintJobRepository.findById(jobId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GenerateLabelPrintJobReportCommand(jobId)))
                .isInstanceOf(LabelPrintJobNotFoundException.class);
    }
}
