package br.com.topone.backend.infrastructure.persistence.jpa;

import br.com.topone.backend.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    java.util.Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
