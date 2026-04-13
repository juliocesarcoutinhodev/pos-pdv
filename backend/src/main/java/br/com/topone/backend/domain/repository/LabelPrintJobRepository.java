package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.LabelPrintJob;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface LabelPrintJobRepository {

    LabelPrintJob save(LabelPrintJob job);

    Optional<LabelPrintJob> findById(UUID id);

    PageResult<LabelPrintJob> findAll(LocalDate referenceDate, int page, int size, PageSort sort);
}
