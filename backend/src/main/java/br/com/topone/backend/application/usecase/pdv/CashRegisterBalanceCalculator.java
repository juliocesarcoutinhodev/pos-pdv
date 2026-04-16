package br.com.topone.backend.application.usecase.pdv;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class CashRegisterBalanceCalculator {

    private CashRegisterBalanceCalculator() {
    }

    public static BigDecimal calculate(
            BigDecimal opening,
            BigDecimal supplies,
            BigDecimal withdrawals,
            BigDecimal salesCashNet
    ) {
        return normalize(opening)
                .add(normalize(supplies))
                .subtract(normalize(withdrawals))
                .add(normalize(salesCashNet))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

