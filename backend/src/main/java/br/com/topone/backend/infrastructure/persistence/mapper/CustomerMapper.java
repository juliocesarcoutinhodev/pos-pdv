package br.com.topone.backend.infrastructure.persistence.mapper;

import br.com.topone.backend.domain.model.Address;
import br.com.topone.backend.domain.model.Customer;
import br.com.topone.backend.infrastructure.persistence.entity.AddressEntity;
import br.com.topone.backend.infrastructure.persistence.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CustomerEntity toEntity(Customer domain);

    Customer toDomain(CustomerEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "address", ignore = true)
    void updateEntity(Customer domain, @MappingTarget CustomerEntity entity);

    AddressEntity toEntity(Address domain);

    Address toDomain(AddressEntity entity);
}
