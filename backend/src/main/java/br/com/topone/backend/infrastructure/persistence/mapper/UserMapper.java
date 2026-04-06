package br.com.topone.backend.infrastructure.persistence.mapper;

import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.domain.model.enums.Role;
import br.com.topone.backend.infrastructure.persistence.entity.RoleEntity;
import br.com.topone.backend.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", imports = {Role.class, RoleEntity.class})
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesDomainToEntitySet")
    UserEntity toEntity(User domain);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesEntityToDomainSet")
    User toDomain(UserEntity entity);

    @Mapping(target = "roles", ignore = true)
    void updateEntity(User domain, @MappingTarget UserEntity entity);

    @Named("mapRolesEntityToDomainSet")
    default Set<Role> mapRolesEntityToDomain(Set<RoleEntity> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(r -> Role.valueOf(r.getName()))
                .collect(Collectors.toSet());
    }

    @Named("mapRolesDomainToEntitySet")
    default Set<RoleEntity> mapRolesDomainToEntity(Set<Role> roles) {
        if (roles == null) return Set.of();
        return roles.stream()
                .map(r -> {
                    var entity = new RoleEntity();
                    entity.setName(r.name());
                    return entity;
                })
                .collect(Collectors.toSet());
    }

    List<User> toDomainList(List<UserEntity> entities);
}
