package br.com.topone.backend.infrastructure.persistence.mapper;

import br.com.topone.backend.domain.model.User;
import br.com.topone.backend.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User domain);

    List<User> toDomainList(List<UserEntity> entities);
}
