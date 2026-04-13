package br.com.topone.backend.application.usecase.product;

import br.com.topone.backend.domain.exception.InvalidProductPricingException;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class ProductPricingCalculator {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int CURRENCY_SCALE = 2;
    private static final int MARGIN_SCALE = 2;
    private static final int DIVISION_SCALE = 6;

    private ProductPricingCalculator() {
    }

    static BigDecimal resolveSalePriceForCreateOrUpdate(BigDecimal costPrice, BigDecimal salePrice, BigDecimal marginPercentage) {
        if (marginPercentage != null) {
            return calculateSalePrice(costPrice, marginPercentage);
        }

        if (salePrice == null) {
            throw new InvalidProductPricingException("Informe o preço de venda ou a margem para calcular o preço de venda.");
        }

        return salePrice;
    }

    static BigDecimal resolveSalePriceForPatch(BigDecimal currentCostPrice, BigDecimal salePrice, BigDecimal marginPercentage) {
        if (marginPercentage != null) {
            return calculateSalePrice(currentCostPrice, marginPercentage);
        }

        return salePrice;
    }

    static BigDecimal calculateMarginPercentage(BigDecimal costPrice, BigDecimal salePrice) {
        if (costPrice == null || salePrice == null) {
            return null;
        }

        if (costPrice.compareTo(ZERO) == 0) {
            if (salePrice.compareTo(ZERO) == 0) {
                return ZERO.setScale(MARGIN_SCALE, RoundingMode.HALF_UP);
            }
            return null;
        }

        return salePrice
                .subtract(costPrice)
                .multiply(HUNDRED)
                .divide(costPrice, MARGIN_SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateSalePrice(BigDecimal costPrice, BigDecimal marginPercentage) {
        if (costPrice == null) {
            throw new InvalidProductPricingException("Informe o preço de custo para calcular o preço de venda a partir da margem.");
        }

        var multiplier = BigDecimal.ONE.add(
                marginPercentage.divide(HUNDRED, DIVISION_SCALE, RoundingMode.HALF_UP)
        );

        return costPrice
                .multiply(multiplier)
                .setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }
}
