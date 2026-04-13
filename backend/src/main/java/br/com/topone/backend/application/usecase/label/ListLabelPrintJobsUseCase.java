package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.repository.LabelPrintJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListLabelPrintJobsUseCase {

    private final LabelPrintJobRepository labelPrintJobRepository;

    @Transactional(readOnly = true)
    public ListLabelPrintJobsResult execute(ListLabelPrintJobsCommand command) {
        var pageResult = labelPrintJobRepository.findAll(
                command.referenceDate(),
                command.page(),
                command.size(),
                command.sort()
        );

        var content = pageResult.content().stream()
                .map(job -> new ListLabelPrintJobsResult.JobSummary(
                        job.getId(),
                        job.getReferenceDate(),
                        job.totalProducts(),
                        job.totalLabels(),
                        job.getCreatedAt()
                ))
                .toList();

        return new ListLabelPrintJobsResult(
                content,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }
}
