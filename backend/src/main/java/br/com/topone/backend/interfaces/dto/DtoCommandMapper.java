package br.com.topone.backend.interfaces.dto;

import br.com.topone.backend.application.usecase.RegisterUserCommand;
import br.com.topone.backend.application.usecase.login.LoginCommand;
import br.com.topone.backend.application.usecase.role.CreateRoleCommand;
import br.com.topone.backend.application.usecase.role.UpdateRoleCommand;
import br.com.topone.backend.application.usecase.role.UpdateRolePatchCommand;
import br.com.topone.backend.application.usecase.customer.CreateCustomerCommand;
import br.com.topone.backend.application.usecase.customer.CustomerAddressCommand;
import br.com.topone.backend.application.usecase.customer.CustomerAddressPatchCommand;
import br.com.topone.backend.application.usecase.customer.UpdateCustomerCommand;
import br.com.topone.backend.application.usecase.customer.UpdateCustomerPatchCommand;
import br.com.topone.backend.application.usecase.product.CreateProductCommand;
import br.com.topone.backend.application.usecase.product.UpdateProductCommand;
import br.com.topone.backend.application.usecase.product.UpdateProductPatchCommand;
import br.com.topone.backend.application.usecase.supplier.CreateSupplierCommand;
import br.com.topone.backend.application.usecase.supplier.SupplierAddressCommand;
import br.com.topone.backend.application.usecase.supplier.SupplierAddressPatchCommand;
import br.com.topone.backend.application.usecase.supplier.SupplierContactCommand;
import br.com.topone.backend.application.usecase.supplier.UpdateSupplierCommand;
import br.com.topone.backend.application.usecase.supplier.UpdateSupplierPatchCommand;
import br.com.topone.backend.application.usecase.user.CreateAdminUserCommand;
import br.com.topone.backend.application.usecase.user.UpdateUserCommand;
import br.com.topone.backend.application.usecase.user.UpdateUserPatchCommand;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface DtoCommandMapper {

    RegisterUserCommand toCommand(RegisterRequest request);

    LoginCommand toLoginCommand(LoginRequest request);

    CreateAdminUserCommand toCreateUserCommand(CreateUserRequest request);

    UpdateUserCommand toUpdateUserCommand(UpdateUserRequest request, UUID id);

    UpdateUserPatchCommand toPatchUserCommand(UpdateUserPatchRequest request, UUID id);

    CreateRoleCommand toCreateRoleCommand(CreateRoleRequest request);

    UpdateRoleCommand toUpdateRoleCommand(UpdateRoleRequest request, UUID id);

    UpdateRolePatchCommand toPatchRoleCommand(UpdateRolePatchRequest request, UUID id);

    CreateSupplierCommand toCreateSupplierCommand(CreateSupplierRequest request);

    UpdateSupplierCommand toUpdateSupplierCommand(UpdateSupplierRequest request, UUID id);

    UpdateSupplierPatchCommand toPatchSupplierCommand(UpdateSupplierPatchRequest request, UUID id);

    CreateCustomerCommand toCreateCustomerCommand(CreateCustomerRequest request);

    UpdateCustomerCommand toUpdateCustomerCommand(UpdateCustomerRequest request, UUID id);

    UpdateCustomerPatchCommand toPatchCustomerCommand(UpdateCustomerPatchRequest request, UUID id);

    CreateProductCommand toCreateProductCommand(CreateProductRequest request);

    UpdateProductCommand toUpdateProductCommand(UpdateProductRequest request, UUID id);

    UpdateProductPatchCommand toPatchProductCommand(UpdateProductPatchRequest request, UUID id);

    SupplierAddressCommand toSupplierAddressCommand(AddressRequest request);

    SupplierAddressPatchCommand toSupplierAddressPatchCommand(AddressPatchRequest request);

    CustomerAddressCommand toCustomerAddressCommand(AddressRequest request);

    CustomerAddressPatchCommand toCustomerAddressPatchCommand(AddressPatchRequest request);

    SupplierContactCommand toSupplierContactCommand(ContactRequest request);
}
