package br.com.topone.backend.infrastructure.persistence.mapper;

import br.com.topone.backend.domain.model.RefreshToken;
import br.com.topone.backend.infrastructure.persistence.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userAgent", ignore = true)
    @Mapping(target = "ipAddress", ignore = true)
    RefreshToken toDomain(RefreshTokenEntity entity);

    @Mapping(target = "user", ignore = true)
    RefreshTokenEntity toEntity(RefreshToken domain);

    List<RefreshToken> toDomainList(List<RefreshTokenEntity> entities);

    List<RefreshTokenEntity> toEntityList(List<RefreshToken> domains);
}
