package br.com.topone.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdvSale {

    private UUID id;
    private UUID cashRegisterSessionId;
    private UUID userId;
    private PdvPaymentMethod paymentMethod;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    private String notes;
    private Instant createdAt;
    private List<PdvSaleItem> items;

    public static PdvSale create(
            UUID sessionId,
            UUID userId,
            PdvPaymentMethod paymentMethod,
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal total,
            BigDecimal paid,
            BigDecimal change,
            String notes,
            List<PdvSaleItem> items
    ) {
        return PdvSale.builder()
                .cashRegisterSessionId(sessionId)
                .userId(userId)
                .paymentMethod(paymentMethod)
                .subtotalAmount(normalizeAmount(subtotal))
                .discountAmount(normalizeAmount(discount))
                .totalAmount(normalizeAmount(total))
                .paidAmount(normalizeAmount(paid))
                .changeAmount(normalizeAmount(change))
                .notes(normalizeNote(notes))
                .items(items)
                .build();
    }

    private static BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String normalizeNote(String value) {
        if (value == null) {
            return null;
        }
        var trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

