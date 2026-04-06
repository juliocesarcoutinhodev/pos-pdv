package br.com.topone.backend.infrastructure.persistence.mapper;

import br.com.topone.backend.domain.model.RefreshToken;
import br.com.topone.backend.infrastructure.persistence.entity.RefreshTokenEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface RefreshTokenMapper {

    RefreshToken toDomain(RefreshTokenEntity entity);

    RefreshTokenEntity toEntity(RefreshToken domain);

    List<RefreshToken> toDomainList(List<RefreshTokenEntity> entities);

    List<RefreshTokenEntity> toEntityList(List<RefreshToken> domains);
}
