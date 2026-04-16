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
public class CashRegisterSession {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private UUID id;
    private UUID userId;
    private BigDecimal openingAmount;
    private Instant openedAt;
    private Instant closedAt;
    private BigDecimal closingAmount;
    private CashRegisterSessionStatus status;

    public static CashRegisterSession open(UUID userId, BigDecimal openingAmount) {
        return CashRegisterSession.builder()
                .userId(userId)
                .openingAmount(normalizeAmount(openingAmount))
                .status(CashRegisterSessionStatus.OPEN)
                .build();
    }

    public boolean isOpen() {
        return CashRegisterSessionStatus.OPEN.equals(status);
    }

    private static BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

