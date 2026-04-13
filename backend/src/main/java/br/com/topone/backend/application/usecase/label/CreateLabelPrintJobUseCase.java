package br.com.topone.backend.application.usecase.label;

import br.com.topone.backend.domain.exception.InvalidLabelPrintJobException;
import br.com.topone.backend.domain.exception.ProductNotFoundException;
import br.com.topone.backend.domain.model.LabelPrintItem;
import br.com.topone.backend.domain.model.LabelPrintJob;
import br.com.topone.backend.domain.repository.LabelPrintJobRepository;
import br.com.topone.backend.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateLabelPrintJobUseCase {

    private final ProductRepository productRepository;
    private final LabelPrintJobRepository labelPrintJobRepository;

    @Transactional
    public CreateLabelPrintJobResult execute(CreateLabelPrintJobCommand command) {
        if (command.items() == null || command.items().isEmpty()) {
            throw new InvalidLabelPrintJobException("Informe ao menos um produto para gerar as etiquetas.");
        }

        var groupedItems = new LinkedHashMap<java.util.UUID, Integer>();
        for (var item : command.items()) {
            if (item.productId() == null) {
                throw new InvalidLabelPrintJobException("Produto inválido na lista de impressão.");
            }
            if (item.quantity() == null || item.quantity() <= 0) {
                throw new InvalidLabelPrintJobException("Quantidade da etiqueta deve ser maior que zero.");
            }

            groupedItems.merge(item.productId(), item.quantity(), Integer::sum);
        }

        var labelItems = new ArrayList<LabelPrintItem>();
        for (var entry : groupedItems.entrySet()) {
            var product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new ProductNotFoundException(entry.getKey()));

            if (!product.isActive()) {
                throw new InvalidLabelPrintJobException("Produto inativo não pode ser incluído na impressão de etiquetas.");
            }

            labelItems.add(LabelPrintItem.create(
                    product.getId(),
                    product.getSku(),
                    product.getBarcode(),
                    product.getName(),
                    product.getUnit(),
                    product.getSalePrice(),
                    product.getPromotionalPrice(),
                    entry.getValue()
            ));
        }

        var job = LabelPrintJob.create(
                command.referenceDate() != null ? command.referenceDate() : LocalDate.now(),
                labelItems
        );

        var saved = labelPrintJobRepository.save(job);
        log.info("Label print job created | id={} | totalProducts={} | totalLabels={}", saved.getId(), saved.totalProducts(), saved.totalLabels());

        return toResult(saved);
    }

    static CreateLabelPrintJobResult toResult(LabelPrintJob job) {
        var items = job.getItems().stream()
                .map(item -> new CreateLabelPrintJobResult.Item(
                        item.getProductId(),
                        item.getSku(),
                        item.getBarcode(),
                        item.getName(),
                        item.getUnit(),
                        item.getSalePrice(),
                        item.getPromotionalPrice(),
                        item.getQuantity()
                ))
                .toList();

        return new CreateLabelPrintJobResult(
                job.getId(),
                job.getReferenceDate(),
                job.totalProducts(),
                job.totalLabels(),
                job.getCreatedAt(),
                items
        );
    }
}
