package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.Role;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByIdIncludingDeleted(UUID id);

    Optional<User> findByEmailIncludingDeleted(String email);

    PageResult<User> findAll(UserFilter filter, int page, int size);

    Set<Role> resolveRolesByIds(Set<UUID> roleIds);
}
