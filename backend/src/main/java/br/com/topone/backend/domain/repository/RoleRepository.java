package br.com.topone.backend.domain.repository;

import br.com.topone.backend.domain.model.Role;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RoleRepository {
    Role save(Role role);
    Optional<Role> findById(UUID id);
    Optional<Role> findByName(String name);
    Optional<Role> findByNameExcludingId(String name, UUID excludeId);
    void deleteById(UUID id);
    PageResult<Role> findAll(int page, int size, PageSort sort);
    Set<Role> resolveByIds(Set<UUID> ids);
    Set<Role> resolveByNames(Set<String> names);
    boolean isAssignedToAnyUser(UUID id);
}
