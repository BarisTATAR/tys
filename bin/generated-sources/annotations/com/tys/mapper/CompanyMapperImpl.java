package com.tys.mapper;

import com.tys.dto.CompanyDto;
import com.tys.model.Company;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.UpdateCompanyRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-20T16:20:27+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.45.0.v20260224-0835, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CompanyMapperImpl implements CompanyMapper {

    @Override
    public Company createCompanyRequestToEntity(CreateCompanyRequest createCompanyRequest) {
        if ( createCompanyRequest == null ) {
            return null;
        }

        Company company = new Company();

        company.setAddress( createCompanyRequest.getAddress() );
        company.setCategory( createCompanyRequest.getCategory() );
        company.setElectricChargeStation( createCompanyRequest.getElectricChargeStation() );
        company.setEmail( createCompanyRequest.getEmail() );
        company.setFacilityNumber( createCompanyRequest.getFacilityNumber() );
        company.setGoogleCommentsUrl( createCompanyRequest.getGoogleCommentsUrl() );
        company.setName( createCompanyRequest.getName() );
        company.setPassword( createCompanyRequest.getPassword() );
        company.setPhoneNumber( createCompanyRequest.getPhoneNumber() );
        company.setStartYear( createCompanyRequest.getStartYear() );
        company.setTaxNumber( createCompanyRequest.getTaxNumber() );
        company.setTotalRoomNumber( createCompanyRequest.getTotalRoomNumber() );
        company.setUsername( createCompanyRequest.getUsername() );

        return company;
    }

    @Override
    public void updateExistingCompanyWithCompanyRequest(UpdateCompanyRequest updateCompanyRequest, Company existingCompany) {
        if ( updateCompanyRequest == null ) {
            return;
        }

        existingCompany.setAddress( updateCompanyRequest.getAddress() );
        existingCompany.setCategory( updateCompanyRequest.getCategory() );
        existingCompany.setElectricChargeStation( updateCompanyRequest.getElectricChargeStation() );
        existingCompany.setEmail( updateCompanyRequest.getEmail() );
        existingCompany.setFacilityNumber( updateCompanyRequest.getFacilityNumber() );
        existingCompany.setGoogleCommentsUrl( updateCompanyRequest.getGoogleCommentsUrl() );
        existingCompany.setId( updateCompanyRequest.getId() );
        existingCompany.setName( updateCompanyRequest.getName() );
        existingCompany.setPhoneNumber( updateCompanyRequest.getPhoneNumber() );
        existingCompany.setStartYear( updateCompanyRequest.getStartYear() );
        existingCompany.setTaxNumber( updateCompanyRequest.getTaxNumber() );
        existingCompany.setTotalRoomNumber( updateCompanyRequest.getTotalRoomNumber() );
    }

    @Override
    public CompanyDto toDto(Company company) {
        if ( company == null ) {
            return null;
        }

        CompanyDto companyDto = new CompanyDto();

        companyDto.setAddress( company.getAddress() );
        companyDto.setCategory( company.getCategory() );
        companyDto.setElectricChargeStation( company.getElectricChargeStation() );
        companyDto.setEmail( company.getEmail() );
        companyDto.setFacilityNumber( company.getFacilityNumber() );
        companyDto.setId( company.getId() );
        companyDto.setName( company.getName() );
        companyDto.setPassword( company.getPassword() );
        companyDto.setPhoneNumber( company.getPhoneNumber() );
        companyDto.setStartYear( company.getStartYear() );
        companyDto.setTaxNumber( company.getTaxNumber() );
        companyDto.setTotalRoomNumber( company.getTotalRoomNumber() );
        companyDto.setUsername( company.getUsername() );

        return companyDto;
    }
}
