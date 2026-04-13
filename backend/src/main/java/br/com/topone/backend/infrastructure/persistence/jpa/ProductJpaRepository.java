package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    @Query("SELECT EXISTS(SELECT 1 FROM ProductEntity p WHERE p.sku = :sku)")
    boolean existsBySku(@Param("sku") String sku);

    @Query("SELECT EXISTS(SELECT 1 FROM ProductEntity p WHERE p.barcode = :barcode)")
    boolean existsByBarcode(@Param("barcode") String barcode);

    @Query("SELECT p FROM ProductEntity p WHERE p.sku = :sku AND p.id != :excludeId")
    Optional<ProductEntity> findBySkuExcludingId(@Param("sku") String sku, @Param("excludeId") UUID excludeId);

    @Query("SELECT p FROM ProductEntity p WHERE p.barcode = :barcode AND p.id != :excludeId")
    Optional<ProductEntity> findByBarcodeExcludingId(@Param("barcode") String barcode, @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT p FROM ProductEntity p
            WHERE (:name IS NULL OR LOWER(p.name) LIKE :name)
              AND (:sku IS NULL OR p.sku LIKE :sku)
              AND (:barcode IS NULL OR p.barcode LIKE :barcode)
              AND (:category IS NULL OR LOWER(p.category) LIKE :category)
              AND (:active IS NULL
                   OR (:active = true  AND p.deletedAt IS NULL)
                   OR (:active = false AND p.deletedAt IS NOT NULL))
            """)
    Page<ProductEntity> searchByFilter(@Param("name") String name,
                                       @Param("sku") String sku,
                                       @Param("barcode") String barcode,
                                       @Param("category") String category,
                                       @Param("active") Boolean active,
                                       Pageable pageable);
}
