package br.com.topone.backend.application.usecase;

import br.com.topone.backend.interfaces.dto.AuthResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UseCaseResponseMapper {

    @Mapping(target = "user.id", source = "id")
    @Mapping(target = "user.email", source = "email")
    @Mapping(target = "user.name", source = "name")
    @Mapping(target = "user.provider", source = "provider")
    AuthResponse toResponse(RegisterUserResult result);
}
