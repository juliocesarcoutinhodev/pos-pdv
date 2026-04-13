package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.exception.LabelPrintJobNotFoundException;
import br.com.topone.backend.domain.repository.LabelPrintJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetLabelPrintJobByIdUseCase {

    private final LabelPrintJobRepository labelPrintJobRepository;

    @Transactional(readOnly = true)
    public CreateLabelPrintJobResult execute(GetLabelPrintJobByIdCommand command) {
        var job = labelPrintJobRepository.findById(command.id())
                .orElseThrow(() -> new LabelPrintJobNotFoundException(command.id()));

        return CreateLabelPrintJobUseCase.toResult(job);
    }
}
