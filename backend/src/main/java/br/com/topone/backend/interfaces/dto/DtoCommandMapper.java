package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.application.usecase.RegisterUserCommand;
import br.com.topone.backend.application.usecase.login.LoginCommand;
import br.com.topone.backend.application.usecase.user.CreateAdminUserCommand;
import br.com.topone.backend.application.usecase.user.CreateAdminUserResult;
import br.com.topone.backend.application.usecase.user.UpdateUserCommand;
import br.com.topone.backend.application.usecase.user.UpdateUserPatchCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DtoCommandMapper {

    RegisterUserCommand toCommand(RegisterRequest request);

    LoginCommand toLoginCommand(LoginRequest request);

    CreateAdminUserCommand toCreateUserCommand(CreateUserRequest request);

    UpdateUserCommand toUpdateUserCommand(UpdateUserRequest request, UUID id);

    UpdateUserPatchCommand toPatchUserCommand(UpdateUserPatchRequest request, UUID id);
}
