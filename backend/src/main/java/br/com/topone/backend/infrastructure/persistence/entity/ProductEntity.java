package br.com.topone.backend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_products", uniqueConstraints = {
        @UniqueConstraint(name = "uq_products_sku", columnNames = "sku"),
        @UniqueConstraint(name = "uq_products_barcode", columnNames = "barcode")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 60)
    private String sku;

    @Column(unique = true, length = 20)
    private String barcode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 120)
    private String brand;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, length = 10)
    private String unit;

    @Column(name = "cost_price", precision = 15, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "sale_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "promotional_price", precision = 15, scale = 2)
    private BigDecimal promotionalPrice;

    @Builder.Default
    @Column(name = "stock_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal stockQuantity = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

    @Builder.Default
    @Column(name = "minimum_stock", nullable = false, precision = 15, scale = 3)
    private BigDecimal minimumStock = BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);

    @Column(length = 8)
    private String ncm;

    @Column(length = 7)
    private String cest;

    @Column(length = 4)
    private String cfop;

    @Column(name = "tax_origin", length = 2)
    private String taxOrigin;

    @Column(name = "tax_situation", length = 10)
    private String taxSituation;

    @Column(name = "icms_rate", precision = 5, scale = 2)
    private BigDecimal icmsRate;

    @Column(name = "pis_situation", length = 4)
    private String pisSituation;

    @Column(name = "pis_rate", precision = 5, scale = 2)
    private BigDecimal pisRate;

    @Column(name = "cofins_situation", length = 4)
    private String cofinsSituation;

    @Column(name = "cofins_rate", precision = 5, scale = 2)
    private BigDecimal cofinsRate;

    @Column(name = "image_id", length = 120)
    private String imageId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
