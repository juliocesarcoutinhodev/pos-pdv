package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.infrastructure.persistence.entity.SupplierEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SupplierJpaRepository extends JpaRepository<SupplierEntity, UUID> {

    @Query("SELECT s FROM SupplierEntity s WHERE s.taxId = :taxId")
    Optional<SupplierEntity> findByTaxId(@Param("taxId") String taxId);

    @Query("SELECT EXISTS(SELECT 1 FROM SupplierEntity s WHERE s.taxId = :taxId)")
    boolean existsByTaxId(@Param("taxId") String taxId);

    @Query("SELECT s FROM SupplierEntity s WHERE s.taxId = :taxId AND s.id != :excludeId")
    Optional<SupplierEntity> findByTaxIdExcludingId(@Param("taxId") String taxId, @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT s FROM SupplierEntity s
            WHERE (:name IS NULL
                   OR LOWER(s.name) LIKE :name
                   OR LOWER(
                        REPLACE(
                            REPLACE(
                                REPLACE(
                                    REPLACE(
                                        REPLACE(s.name, '.', ''),
                                    '-', ''),
                                '_', ''),
                            ' ', ''),
                        '/', '')
                   ) LIKE :normalizedName)
              AND (:taxId IS NULL OR s.taxId LIKE :taxId)
              AND (:email IS NULL OR LOWER(s.email) LIKE :email)
              AND (:active IS NULL
                   OR (:active = true  AND s.deletedAt IS NULL)
                   OR (:active = false AND s.deletedAt IS NOT NULL))
            """)
    Page<SupplierEntity> searchByFilter(@Param("name") String name,
                                        @Param("normalizedName") String normalizedName,
                                        @Param("taxId") String taxId,
                                        @Param("email") String email,
                                        @Param("active") Boolean active,
                                        Pageable pageable);
}
