package br.com.topone.backend.domain.model;

import br.com.topone.backend.domain.exception.InvalidLabelPrintJobException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabelPrintItem {

    private UUID id;
    private UUID productId;
    private String sku;
    private String barcode;
    private String name;
    private String unit;
    private BigDecimal salePrice;
    private BigDecimal promotionalPrice;
    private Integer quantity;

    public static LabelPrintItem create(
            UUID productId,
            String sku,
            String barcode,
            String name,
            String unit,
            BigDecimal salePrice,
            BigDecimal promotionalPrice,
            Integer quantity
    ) {
        return LabelPrintItem.builder()
                .productId(normalizeProductId(productId))
                .sku(Product.normalizeSku(sku))
                .barcode(Product.normalizeBarcode(barcode))
                .name(normalizeName(name))
                .unit(normalizeUnit(unit))
                .salePrice(normalizeAmount(salePrice, "Preço de venda é obrigatório para impressão de etiqueta"))
                .promotionalPrice(normalizeOptionalAmount(promotionalPrice))
                .quantity(normalizeQuantity(quantity))
                .build();
    }

    private static UUID normalizeProductId(UUID productId) {
        if (productId == null) {
            throw new InvalidLabelPrintJobException("Produto inválido na lista de impressão.");
        }
        return productId;
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidLabelPrintJobException("Nome do produto é obrigatório para impressão de etiqueta.");
        }
        return name.trim();
    }

    private static String normalizeUnit(String unit) {
        if (unit == null || unit.isBlank()) {
            throw new InvalidLabelPrintJobException("Unidade do produto é obrigatória para impressão de etiqueta.");
        }
        return unit.trim().toUpperCase(Locale.ROOT);
    }

    private static BigDecimal normalizeAmount(BigDecimal value, String message) {
        if (value == null) {
            throw new InvalidLabelPrintJobException(message);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizeOptionalAmount(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static Integer normalizeQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidLabelPrintJobException("Quantidade da etiqueta deve ser maior que zero.");
        }
        return quantity;
    }
}
