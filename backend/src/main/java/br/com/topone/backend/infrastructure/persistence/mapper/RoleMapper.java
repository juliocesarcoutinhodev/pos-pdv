package br.com.topone.backend.infrastructure.persistence.mapper;

import br.com.topone.backend.domain.model.Role;
import br.com.topone.backend.infrastructure.persistence.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    RoleEntity toEntity(Role domain);

    Role toDomain(RoleEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(Role domain, @MappingTarget RoleEntity entity);

    List<Role> toDomainList(List<RoleEntity> entities);

    default Set<String> toRoleNameSet(Set<RoleEntity> entities) {
        if (entities == null) {
            return Set.of();
        }

        return entities.stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
    }

    default Set<Role> toDomainRoleSet(Set<RoleEntity> entities) {
        if (entities == null) return Set.of();
        return entities.stream().map(this::toDomain).collect(Collectors.toSet());
    }
}
