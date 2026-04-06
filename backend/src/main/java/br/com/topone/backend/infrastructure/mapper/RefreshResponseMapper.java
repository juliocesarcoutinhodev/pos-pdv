package br.com.topone.backend.infrastructure.mapper;

import br.com.topone.backend.application.usecase.login.RefreshTokenResult;
import br.com.topone.backend.interfaces.dto.RefreshResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RefreshResponseMapper {

    @Mapping(target = "accessToken", source = "accessToken")
    @Mapping(target = "expiresIn", source = "expiresIn")
    RefreshResponse toResponse(RefreshTokenResult result);
}
