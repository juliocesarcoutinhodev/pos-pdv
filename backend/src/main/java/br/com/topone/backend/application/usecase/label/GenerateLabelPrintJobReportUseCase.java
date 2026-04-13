package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.exception.LabelPrintJobNotFoundException;
import br.com.topone.backend.domain.repository.LabelPrintJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GenerateLabelPrintJobReportUseCase {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final LabelPrintJobRepository labelPrintJobRepository;
    private final LabelReportGateway labelReportGateway;

    @Transactional(readOnly = true)
    public GenerateLabelPrintJobReportResult execute(GenerateLabelPrintJobReportCommand command) {
        var job = labelPrintJobRepository.findById(command.jobId())
                .orElseThrow(() -> new LabelPrintJobNotFoundException(command.jobId()));

        var content = labelReportGateway.generateJobReportPdf(job.getId().toString());
        return new GenerateLabelPrintJobReportResult(
                "etiquetas-" + job.getId() + ".pdf",
                PDF_CONTENT_TYPE,
                content
        );
    }
}
