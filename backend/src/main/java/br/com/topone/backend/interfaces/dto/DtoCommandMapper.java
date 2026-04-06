package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.application.usecase.LoginCommand;
import br.com.topone.backend.application.usecase.RegisterUserCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DtoCommandMapper {

    RegisterUserCommand toCommand(RegisterRequest request);

    LoginCommand toLoginCommand(LoginRequest request);
}
