package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.infrastructure.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByName(String name);

    @Query("SELECT r FROM RoleEntity r WHERE r.name = :name AND r.id != :excludeId")
    Optional<RoleEntity> findByNameExcludingId(@Param("name") String name, @Param("excludeId") UUID excludeId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserEntity u JOIN u.roles r WHERE r.id = :id")
    boolean existsAssignedUserByRoleId(@Param("id") UUID id);

    java.util.List<RoleEntity> findByIdIn(Collection<UUID> ids);
}
