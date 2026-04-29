package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL")
    java.util.Optional<UserEntity> findByEmail(String email);

    @Query("SELECT EXISTS(SELECT 1 FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL)")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u WHERE u.id = :id AND u.deletedAt IS NULL")
    java.util.Optional<UserEntity> findByIdActive(@Param("id") UUID id);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.deletedAt IS NULL AND u.id != :excludeId")
    java.util.Optional<UserEntity> findActiveByEmailExcludingId(@Param("email") String email, @Param("excludeId") UUID excludeId);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email AND u.id != :excludeId")
    java.util.Optional<UserEntity> findByEmailExcludingId(@Param("email") String email, @Param("excludeId") UUID excludeId);

    @Query("SELECT u FROM UserEntity u WHERE u.id = :id")
    java.util.Optional<UserEntity> findByIdIncludingDeleted(@Param("id") UUID id);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
    java.util.Optional<UserEntity> findByEmailIncludingDeleted(@Param("email") String email);

    @Query("""
            SELECT u FROM UserEntity u
            WHERE (:name IS NULL OR LOWER(u.name) LIKE :name)
              AND (:email IS NULL OR LOWER(u.email) LIKE :email)
              AND (:active IS NULL
                   OR (:active = true  AND u.deletedAt IS NULL)
                   OR (:active = false AND u.deletedAt IS NOT NULL))
            """)
    Page<UserEntity> searchByFilter(@Param("name") String name,
                                    @Param("email") String email,
                                    @Param("active") Boolean active,
                                    Pageable pageable);

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.deletedAt IS NULL")
    long countActiveUsers();
}
