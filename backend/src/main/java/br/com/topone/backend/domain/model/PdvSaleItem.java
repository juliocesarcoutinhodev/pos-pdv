package br.com.topone.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdvSaleItem {

    private UUID id;
    private UUID productId;
    private String sku;
    private String barcode;
    private String name;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal lineTotal;

    public static PdvSaleItem fromProduct(Product product, BigDecimal quantity) {
        var normalizedQuantity = normalizeQuantity(quantity);
        var unitPrice = resolveUnitPrice(product);
        return PdvSaleItem.builder()
                .productId(product.getId())
                .sku(product.getSku())
                .barcode(product.getBarcode())
                .name(product.getName())
                .unit(product.getUnit())
                .unitPrice(unitPrice)
                .quantity(normalizedQuantity)
                .lineTotal(unitPrice.multiply(normalizedQuantity).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private static BigDecimal resolveUnitPrice(Product product) {
        if (product.getPromotionalPrice() != null && product.getPromotionalPrice().compareTo(BigDecimal.ZERO) > 0) {
            return product.getPromotionalPrice();
        }
        return product.getSalePrice();
    }

    private static BigDecimal normalizeQuantity(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }
}

