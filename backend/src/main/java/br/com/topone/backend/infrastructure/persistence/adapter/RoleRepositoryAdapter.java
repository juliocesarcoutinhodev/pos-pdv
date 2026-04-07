package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.exception.RoleNotFoundException;
import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.domain.repository.PageResult;
import br.com.topone.backend.domain.repository.RoleRepository;
import br.com.topone.backend.infrastructure.persistence.entity.RoleEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.RoleJpaRepository;
import br.com.topone.backend.infrastructure.persistence.mapper.RoleMapper;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository roleJpaRepository;
    private final RoleMapper mapper;

    public RoleRepositoryAdapter(RoleJpaRepository roleJpaRepository,
                                 RoleMapper mapper) {
        this.roleJpaRepository = roleJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Role save(Role role) {
        RoleEntity entity;
        if (role.getId() != null) {
            entity = roleJpaRepository.findById(role.getId())
                    .orElseThrow(RoleNotFoundException::new);
            mapper.updateEntity(role, entity);
        } else {
            entity = mapper.toEntity(role);
        }
        var saved = roleJpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Role> findById(UUID id) {
        return roleJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByNameExcludingId(String name, UUID excludeId) {
        return roleJpaRepository.findByNameExcludingId(name, excludeId).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        roleJpaRepository.deleteById(id);
    }

    @Override
    public PageResult<Role> findAll(int page, int size) {
        var springPage = org.springframework.data.domain.PageRequest.of(page, size);
        var pageResult = roleJpaRepository.findAll(springPage);
        var roles = pageResult.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(
                roles, pageResult.getNumber(), pageResult.getSize(),
                pageResult.getTotalElements(), pageResult.getTotalPages());
    }

    @Override
    public Set<Role> resolveByIds(Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        var resolved = roleJpaRepository.findByIdIn(ids).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());

        if (resolved.size() != ids.size()) {
            throw new RoleNotFoundException();
        }

        return resolved;
    }

    @Override
    public Set<Role> resolveByNames(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of();
        }

        var normalizedNames = names.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());

        var resolved = new HashSet<Role>();
        for (var name : normalizedNames) {
            roleJpaRepository.findByName(name).map(mapper::toDomain).ifPresent(resolved::add);
        }
        if (resolved.size() != normalizedNames.size()) {
            throw new RoleNotFoundException();
        }
        return resolved;
    }

    @Override
    public boolean isAssignedToAnyUser(UUID id) {
        return roleJpaRepository.existsAssignedUserByRoleId(id);
    }
}
