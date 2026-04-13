package br.com.topone.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    private static final BigDecimal ZERO_AMOUNT = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    private static final BigDecimal ZERO_QUANTITY = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

    private UUID id;
    private String sku;
    private String barcode;
    private String name;
    private String description;
    private String brand;
    private String category;
    private UUID supplierId;
    private String unit;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private BigDecimal promotionalPrice;
    private BigDecimal stockQuantity;
    private BigDecimal minimumStock;
    private String ncm;
    private String cest;
    private String cfop;
    private String taxOrigin;
    private String taxSituation;
    private BigDecimal icmsRate;
    private String pisSituation;
    private BigDecimal pisRate;
    private String cofinsSituation;
    private BigDecimal cofinsRate;
    private String imageId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public static Product create(
            String sku,
            String barcode,
            String name,
            String description,
            String brand,
            String category,
            UUID supplierId,
            String unit,
            BigDecimal costPrice,
            BigDecimal salePrice,
            BigDecimal promotionalPrice,
            BigDecimal stockQuantity,
            BigDecimal minimumStock,
            String ncm,
            String cest,
            String cfop,
            String taxOrigin,
            String taxSituation,
            BigDecimal icmsRate,
            String pisSituation,
            BigDecimal pisRate,
            String cofinsSituation,
            BigDecimal cofinsRate,
            String imageId
    ) {
        var product = new Product();
        product.sku = normalizeSku(sku);
        product.barcode = normalizeBarcode(barcode);
        product.name = normalizeName(name);
        product.description = normalizeOptionalText(description);
        product.brand = normalizeOptionalText(brand);
        product.category = normalizeOptionalText(category);
        product.supplierId = normalizeSupplierId(supplierId);
        product.unit = normalizeUnit(unit);
        product.costPrice = normalizeAmount(costPrice, true);
        product.salePrice = normalizeAmount(salePrice, false);
        product.promotionalPrice = normalizeAmount(promotionalPrice, true);
        product.stockQuantity = normalizeQuantity(stockQuantity);
        product.minimumStock = normalizeQuantity(minimumStock);
        product.ncm = normalizeDigitsCode(ncm);
        product.cest = normalizeDigitsCode(cest);
        product.cfop = normalizeDigitsCode(cfop);
        product.taxOrigin = normalizeTaxOrigin(taxOrigin);
        product.taxSituation = normalizeTaxSituation(taxSituation);
        product.icmsRate = normalizeRate(icmsRate);
        product.pisSituation = normalizeTaxSituation(pisSituation);
        product.pisRate = normalizeRate(pisRate);
        product.cofinsSituation = normalizeTaxSituation(cofinsSituation);
        product.cofinsRate = normalizeRate(cofinsRate);
        product.imageId = normalizeOptionalText(imageId);
        return product;
    }

    public void changeSku(String sku) {
        this.sku = normalizeSku(sku);
    }

    public void changeBarcode(String barcode) {
        this.barcode = normalizeBarcode(barcode);
    }

    public void changeName(String name) {
        this.name = normalizeName(name);
    }

    public void changeDescription(String description) {
        this.description = normalizeOptionalText(description);
    }

    public void changeBrand(String brand) {
        this.brand = normalizeOptionalText(brand);
    }

    public void changeCategory(String category) {
        this.category = normalizeOptionalText(category);
    }

    public void changeSupplierId(UUID supplierId) {
        this.supplierId = normalizeSupplierId(supplierId);
    }

    public void changeUnit(String unit) {
        this.unit = normalizeUnit(unit);
    }

    public void changeCostPrice(BigDecimal costPrice) {
        this.costPrice = normalizeAmount(costPrice, true);
    }

    public void changeSalePrice(BigDecimal salePrice) {
        this.salePrice = normalizeAmount(salePrice, false);
    }

    public void changePromotionalPrice(BigDecimal promotionalPrice) {
        this.promotionalPrice = normalizeAmount(promotionalPrice, true);
    }

    public void changeStockQuantity(BigDecimal stockQuantity) {
        this.stockQuantity = normalizeQuantity(stockQuantity);
    }

    public void changeMinimumStock(BigDecimal minimumStock) {
        this.minimumStock = normalizeQuantity(minimumStock);
    }

    public void changeNcm(String ncm) {
        this.ncm = normalizeDigitsCode(ncm);
    }

    public void changeCest(String cest) {
        this.cest = normalizeDigitsCode(cest);
    }

    public void changeCfop(String cfop) {
        this.cfop = normalizeDigitsCode(cfop);
    }

    public void changeTaxOrigin(String taxOrigin) {
        this.taxOrigin = normalizeTaxOrigin(taxOrigin);
    }

    public void changeTaxSituation(String taxSituation) {
        this.taxSituation = normalizeTaxSituation(taxSituation);
    }

    public void changeIcmsRate(BigDecimal icmsRate) {
        this.icmsRate = normalizeRate(icmsRate);
    }

    public void changePisSituation(String pisSituation) {
        this.pisSituation = normalizeTaxSituation(pisSituation);
    }

    public void changePisRate(BigDecimal pisRate) {
        this.pisRate = normalizeRate(pisRate);
    }

    public void changeCofinsSituation(String cofinsSituation) {
        this.cofinsSituation = normalizeTaxSituation(cofinsSituation);
    }

    public void changeCofinsRate(BigDecimal cofinsRate) {
        this.cofinsRate = normalizeRate(cofinsRate);
    }

    public void changeImageId(String imageId) {
        this.imageId = normalizeOptionalText(imageId);
    }

    public void deactivate() {
        this.deletedAt = Instant.now();
    }

    public void reactivate() {
        this.deletedAt = null;
    }

    public boolean isActive() {
        return deletedAt == null;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public static String normalizeSku(String sku) {
        if (sku == null) {
            return null;
        }
        var normalized = sku.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    public static String normalizeBarcode(String barcode) {
        if (barcode == null) {
            return null;
        }
        var normalized = barcode.replaceAll("\\D", "");
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeName(String name) {
        return name == null ? null : name.trim();
    }

    private static UUID normalizeSupplierId(UUID supplierId) {
        return supplierId;
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeUnit(String unit) {
        if (unit == null) {
            return null;
        }
        var normalized = unit.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeDigitsCode(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.replaceAll("\\D", "");
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeTaxOrigin(String taxOrigin) {
        if (taxOrigin == null) {
            return null;
        }
        var normalized = taxOrigin.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeTaxSituation(String taxSituation) {
        if (taxSituation == null) {
            return null;
        }
        var normalized = taxSituation.trim().toUpperCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    private static BigDecimal normalizeAmount(BigDecimal value, boolean allowNull) {
        if (value == null) {
            return allowNull ? null : ZERO_AMOUNT;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizeQuantity(BigDecimal value) {
        if (value == null) {
            return ZERO_QUANTITY;
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private static BigDecimal normalizeRate(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
