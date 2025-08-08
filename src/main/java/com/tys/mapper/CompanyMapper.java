package com.tys.mapper;

import com.tys.model.Company;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.UpdateCompanyRequest;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {
    Company createCompanyRequestToEntity(CreateCompanyRequest createCompanyRequest);

    //@Mapping(source = "id", target = "id", ignore = true)
    void updateExistingCompanyWithCompanyRequest(UpdateCompanyRequest updateCompanyRequest, @MappingTarget Company existingCompany);
}

