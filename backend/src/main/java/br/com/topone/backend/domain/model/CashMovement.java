package br.com.topone.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashMovement {

    private UUID id;
    private UUID cashRegisterSessionId;
    private UUID userId;
    private CashMovementType type;
    private BigDecimal amount;
    private String note;
    private Instant createdAt;

    public static CashMovement create(UUID sessionId, UUID userId, CashMovementType type, BigDecimal amount, String note) {
        return CashMovement.builder()
                .cashRegisterSessionId(sessionId)
                .userId(userId)
                .type(type)
                .amount(normalizeAmount(amount))
                .note(normalizeNote(note))
                .build();
    }

    private static BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            return null;
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

