package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.exception.LabelPrintJobNotFoundException;
import br.com.topone.backend.domain.model.LabelPrintItem;
import br.com.topone.backend.domain.model.LabelPrintJob;
import br.com.topone.backend.domain.repository.LabelPrintJobRepository;
import br.com.topone.backend.domain.repository.PageResult;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.SortDirection;
import br.com.topone.backend.infrastructure.persistence.entity.LabelPrintJobEntity;
import br.com.topone.backend.infrastructure.persistence.entity.LabelPrintItemEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.LabelPrintJobJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LabelPrintJobRepositoryAdapter implements LabelPrintJobRepository {

    private final LabelPrintJobJpaRepository labelPrintJobJpaRepository;

    public LabelPrintJobRepositoryAdapter(LabelPrintJobJpaRepository labelPrintJobJpaRepository) {
        this.labelPrintJobJpaRepository = labelPrintJobJpaRepository;
    }

    @Override
    public LabelPrintJob save(LabelPrintJob job) {
        var entity = job.getId() != null
                ? labelPrintJobJpaRepository.findById(job.getId()).orElseThrow(() -> new LabelPrintJobNotFoundException(job.getId()))
                : new LabelPrintJobEntity();

        entity.setReferenceDate(job.getReferenceDate());
        entity.getItems().clear();

        for (var item : job.getItems()) {
            var itemEntity = LabelPrintItemEntity.builder()
                    .id(item.getId())
                    .job(entity)
                    .productId(item.getProductId())
                    .sku(item.getSku())
                    .barcode(item.getBarcode())
                    .name(item.getName())
                    .unit(item.getUnit())
                    .salePrice(item.getSalePrice())
                    .promotionalPrice(item.getPromotionalPrice())
                    .quantity(item.getQuantity())
                    .build();
            entity.getItems().add(itemEntity);
        }

        var saved = labelPrintJobJpaRepository.saveAndFlush(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<LabelPrintJob> findById(UUID id) {
        return labelPrintJobJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<LabelPrintJob> findAll(LocalDate referenceDate, int page, int size, PageSort sort) {
        var sortSpec = sort != null && sort.isSorted()
                ? Sort.by(
                sort.direction() == SortDirection.DESC
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                sort.field())
                : Sort.unsorted();

        var springPage = PageRequest.of(page, size, sortSpec);
        var pageResult = referenceDate != null
                ? labelPrintJobJpaRepository.findAllByReferenceDate(referenceDate, springPage)
                : labelPrintJobJpaRepository.findAll(springPage);

        var jobs = pageResult.getContent().stream()
                .map(this::toDomain)
                .toList();

        return new PageResult<>(
                jobs,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages()
        );
    }

    private LabelPrintJob toDomain(LabelPrintJobEntity entity) {
        var items = entity.getItems().stream()
                .map(item -> LabelPrintItem.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .sku(item.getSku())
                        .barcode(item.getBarcode())
                        .name(item.getName())
                        .unit(item.getUnit())
                        .salePrice(item.getSalePrice())
                        .promotionalPrice(item.getPromotionalPrice())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        return LabelPrintJob.builder()
                .id(entity.getId())
                .referenceDate(entity.getReferenceDate())
                .items(new ArrayList<>(items))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
