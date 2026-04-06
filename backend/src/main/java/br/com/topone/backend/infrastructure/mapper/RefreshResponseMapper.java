package br.com.topone.backend.infrastructure.mapper;

import br.com.topone.backend.application.usecase.RefreshTokenResult;
import br.com.topone.backend.interfaces.dto.RefreshResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefreshResponseMapper {

    RefreshResponse toResponse(RefreshTokenResult result);
}
