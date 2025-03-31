package com.tys.mapper;

import com.tys.model.Company;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.UpdateCompanyRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CompanyMapper {
    Company createCompanyRequestToEntity(CreateCompanyRequest createCompanyRequest);

    @Mapping(source = "id", target = "id", ignore = true)
    void updateExistingCompanyWithCompanyRequest(UpdateCompanyRequest updateCompanyRequest, @MappingTarget Company existingCompany);
}

