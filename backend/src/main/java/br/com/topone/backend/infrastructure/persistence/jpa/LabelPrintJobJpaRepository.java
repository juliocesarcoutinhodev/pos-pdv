package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.infrastructure.persistence.entity.LabelPrintJobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface LabelPrintJobJpaRepository extends JpaRepository<LabelPrintJobEntity, UUID> {

    Page<LabelPrintJobEntity> findAllByReferenceDate(LocalDate referenceDate, Pageable pageable);
}
