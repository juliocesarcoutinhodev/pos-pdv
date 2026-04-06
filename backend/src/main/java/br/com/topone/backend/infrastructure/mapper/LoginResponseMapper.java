package br.com.topone.backend.infrastructure.mapper;

import br.com.topone.backend.application.usecase.LoginResult;
import br.com.topone.backend.interfaces.dto.LoginResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoginResponseMapper {

    @Mapping(target = "user.id", source = "id")
    @Mapping(target = "user.email", source = "email")
    @Mapping(target = "user.name", source = "name")
    @Mapping(target = "user.provider", source = "provider")
    LoginResponse toResponse(LoginResult result);
}
