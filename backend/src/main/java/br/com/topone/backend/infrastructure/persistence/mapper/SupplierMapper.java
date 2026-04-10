package br.com.topone.backend.infrastructure.persistence.mapper;

import br.com.topone.backend.domain.model.Address;
import br.com.topone.backend.domain.model.Contact;
import br.com.topone.backend.domain.model.Supplier;
import br.com.topone.backend.infrastructure.persistence.entity.AddressEntity;
import br.com.topone.backend.infrastructure.persistence.entity.ContactEntity;
import br.com.topone.backend.infrastructure.persistence.entity.SupplierEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SupplierEntity toEntity(Supplier domain);

    Supplier toDomain(SupplierEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    void updateEntity(Supplier domain, @MappingTarget SupplierEntity entity);

    AddressEntity toEntity(Address domain);

    Address toDomain(AddressEntity entity);

    ContactEntity toEntity(Contact domain);

    Contact toDomain(ContactEntity entity);

    List<ContactEntity> toContactEntityList(List<Contact> contacts);

    List<Contact> toContactDomainList(List<ContactEntity> contacts);
}
