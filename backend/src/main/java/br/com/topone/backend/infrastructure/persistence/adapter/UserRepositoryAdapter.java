package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.exception.UserNotFoundException;
import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.repository.PageSort;
import br.com.topone.backend.domain.repository.UserRepository;
import br.com.topone.backend.infrastructure.persistence.entity.RoleEntity;
import br.com.topone.backend.infrastructure.persistence.entity.UserEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.RoleJpaRepository;
import br.com.topone.backend.infrastructure.persistence.jpa.UserJpaRepository;
import br.com.topone.backend.infrastructure.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final UserMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository,
                                 RoleJpaRepository roleJpaRepository,
                                 UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.roleJpaRepository = roleJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        var roleEntities = resolveRoles(user.getRoles());
        UserEntity entity;

        if (user.getId() != null) {
            entity = jpaRepository.findByIdIncludingDeleted(user.getId())
                    .orElseThrow(UserNotFoundException::new);
            mapper.updateEntity(user, entity);
        } else {
            entity = mapper.toEntity(user);
        }

        entity.getRoles().clear();
        entity.getRoles().addAll(new HashSet<>(roleEntities));
        var saved = jpaRepository.saveAndFlush(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findByIdIncludingDeleted(UUID id) {
        return jpaRepository.findByIdIncludingDeleted(id).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmailIncludingDeleted(String email) {
        return jpaRepository.findByEmailIncludingDeleted(email).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findActiveByEmailExcludingId(String email, UUID excludeId) {
        return jpaRepository.findActiveByEmailExcludingId(email, excludeId).map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmailExcludingId(String email, UUID excludeId) {
        return jpaRepository.findByEmailExcludingId(email, excludeId).map(mapper::toDomain);
    }

    @Override
    public br.com.topone.backend.domain.repository.PageResult<User> findAll(
            br.com.topone.backend.domain.repository.UserFilter filter,
            int page,
            int size,
            PageSort sort) {
        var namePattern = filter.name() != null && !filter.name().isBlank()
                ? "%" + filter.name().toLowerCase() + "%" : null;
        var emailPattern = filter.email() != null && !filter.email().isBlank()
                ? "%" + filter.email().toLowerCase() + "%" : null;
        var sortSpec = sort != null && sort.isSorted()
                ? org.springframework.data.domain.Sort.by(
                        sort.direction() == br.com.topone.backend.domain.repository.SortDirection.DESC
                                ? org.springframework.data.domain.Sort.Direction.DESC
                                : org.springframework.data.domain.Sort.Direction.ASC,
                        sort.field())
                : org.springframework.data.domain.Sort.unsorted();
        var springPage = org.springframework.data.domain.PageRequest.of(page, size, sortSpec);
        var pageResult = jpaRepository.searchByFilter(namePattern, emailPattern, filter.active(), springPage);
        var users = pageResult.getContent().stream().map(mapper::toDomain).toList();
        return new br.com.topone.backend.domain.repository.PageResult<>(
                users, pageResult.getNumber(), pageResult.getSize(),
                pageResult.getTotalElements(), pageResult.getTotalPages());
    }

    private List<br.com.topone.backend.infrastructure.persistence.entity.RoleEntity> resolveRoles(Set<Role> roles) {
        var resolved = new ArrayList<br.com.topone.backend.infrastructure.persistence.entity.RoleEntity>();
        if (roles == null) return resolved;
        for (var role : roles) {
            roleJpaRepository.findById(role.getId()).ifPresent(resolved::add);
        }
        return resolved;
    }
}
