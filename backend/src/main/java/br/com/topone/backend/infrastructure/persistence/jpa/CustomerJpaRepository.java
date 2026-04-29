package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {

    @Query("SELECT EXISTS(SELECT 1 FROM CustomerEntity c WHERE c.taxId = :taxId)")
    boolean existsByTaxId(@Param("taxId") String taxId);

    @Query("SELECT c FROM CustomerEntity c WHERE c.taxId = :taxId AND c.id != :excludeId")
    Optional<CustomerEntity> findByTaxIdExcludingId(@Param("taxId") String taxId, @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT c FROM CustomerEntity c
            WHERE (:name IS NULL OR LOWER(c.name) LIKE :name)
              AND (:taxId IS NULL OR c.taxId LIKE :taxId)
              AND (:email IS NULL OR LOWER(c.email) LIKE :email)
              AND (:active IS NULL
                   OR (:active = true  AND c.deletedAt IS NULL)
                   OR (:active = false AND c.deletedAt IS NOT NULL))
            """)
    Page<CustomerEntity> searchByFilter(@Param("name") String name,
                                        @Param("taxId") String taxId,
                                        @Param("email") String email,
                                        @Param("active") Boolean active,
                                        Pageable pageable);

    @Query("SELECT COUNT(c) FROM CustomerEntity c WHERE c.deletedAt IS NULL")
    long countActiveCustomers();
}
