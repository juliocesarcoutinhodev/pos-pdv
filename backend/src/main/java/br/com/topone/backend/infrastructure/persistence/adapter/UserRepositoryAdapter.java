package br.com.topone.backend.infrastructure.persistence.adapter;

import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.repository.UserRepository;
import br.com.topone.backend.infrastructure.persistence.entity.RoleEntity;
import br.com.topone.backend.infrastructure.persistence.jpa.RoleJpaRepository;
import br.com.topone.backend.infrastructure.persistence.jpa.UserJpaRepository;
import br.com.topone.backend.infrastructure.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
        var entity = mapper.toEntity(user);
        entity.setRoles(new HashSet<>(roleEntities));
        var saved = jpaRepository.save(entity);
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

    private List<RoleEntity> resolveRoles(java.util.Set<br.com.topone.backend.domain.model.enums.Role> roles) {
        var resolved = new ArrayList<RoleEntity>();
        if (roles == null) return resolved;
        for (var role : roles) {
            roleJpaRepository.findByName(role.name()).ifPresent(resolved::add);
        }
        return resolved;
    }
}
