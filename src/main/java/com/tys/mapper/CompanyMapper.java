package com.tys.mapper;

import com.tys.dto.CompanyDto;
import com.tys.dto.RoomDto;
import com.tys.model.Company;
import com.tys.model.Room;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.UpdateCompanyRequest;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {
    Company createCompanyRequestToEntity(CreateCompanyRequest createCompanyRequest);
    void updateExistingCompanyWithCompanyRequest(UpdateCompanyRequest updateCompanyRequest, @MappingTarget Company existingCompany);

    CompanyDto toDto(Company company);
}

