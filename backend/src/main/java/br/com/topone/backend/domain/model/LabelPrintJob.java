package br.com.topone.backend.domain.model;

import br.com.topone.backend.domain.exception.InvalidLabelPrintJobException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelPrintJob {

    private UUID id;
    private LocalDate referenceDate;
    @Builder.Default
    private List<LabelPrintItem> items = new ArrayList<>();
    private Instant createdAt;

    public static LabelPrintJob create(LocalDate referenceDate, List<LabelPrintItem> items) {
        return LabelPrintJob.builder()
                .referenceDate(normalizeReferenceDate(referenceDate))
                .items(normalizeItems(items))
                .build();
    }

    public int totalProducts() {
        return items == null ? 0 : items.size();
    }

    public int totalLabels() {
        if (items == null) {
            return 0;
        }

        return items.stream()
                .map(LabelPrintItem::getQuantity)
                .filter(quantity -> quantity != null && quantity > 0)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private static LocalDate normalizeReferenceDate(LocalDate referenceDate) {
        return referenceDate == null ? LocalDate.now() : referenceDate;
    }

    private static List<LabelPrintItem> normalizeItems(List<LabelPrintItem> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidLabelPrintJobException("Informe ao menos um produto para gerar as etiquetas.");
        }
        return new ArrayList<>(items);
    }
}
