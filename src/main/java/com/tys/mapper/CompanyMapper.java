package com.tys.mapper;

import com.tys.dto.CompanyDto;
import com.tys.model.Company;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.UpdateCompanyRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roomList", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "kbs", ignore = true)
    Company createCompanyRequestToEntity(CreateCompanyRequest createCompanyRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roomList", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    @Mapping(target = "kbs", ignore = true)
    void updateExistingCompanyWithCompanyRequest(UpdateCompanyRequest updateCompanyRequest, @MappingTarget Company existingCompany);

    @Mapping(target = "kbsConfigured", ignore = true)
    @Mapping(target = "kbsTesisKodu", ignore = true)
    CompanyDto toDto(Company company);
}
