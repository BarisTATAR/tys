package com.tys.mapper;

import com.tys.dto.CompanyDto;
import com.tys.model.Company;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.UpdateCompanyRequest;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-03T19:31:26+0300",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
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

        if ( updateCompanyRequest.getAddress() != null ) {
            existingCompany.setAddress( updateCompanyRequest.getAddress() );
        }
        if ( updateCompanyRequest.getCategory() != null ) {
            existingCompany.setCategory( updateCompanyRequest.getCategory() );
        }
        if ( updateCompanyRequest.getElectricChargeStation() != null ) {
            existingCompany.setElectricChargeStation( updateCompanyRequest.getElectricChargeStation() );
        }
        if ( updateCompanyRequest.getEmail() != null ) {
            existingCompany.setEmail( updateCompanyRequest.getEmail() );
        }
        if ( updateCompanyRequest.getFacilityNumber() != null ) {
            existingCompany.setFacilityNumber( updateCompanyRequest.getFacilityNumber() );
        }
        if ( updateCompanyRequest.getGoogleCommentsUrl() != null ) {
            existingCompany.setGoogleCommentsUrl( updateCompanyRequest.getGoogleCommentsUrl() );
        }
        if ( updateCompanyRequest.getId() != null ) {
            existingCompany.setId( updateCompanyRequest.getId() );
        }
        if ( updateCompanyRequest.getName() != null ) {
            existingCompany.setName( updateCompanyRequest.getName() );
        }
        if ( updateCompanyRequest.getPassword() != null ) {
            existingCompany.setPassword( updateCompanyRequest.getPassword() );
        }
        if ( updateCompanyRequest.getPhoneNumber() != null ) {
            existingCompany.setPhoneNumber( updateCompanyRequest.getPhoneNumber() );
        }
        if ( updateCompanyRequest.getStartYear() != null ) {
            existingCompany.setStartYear( updateCompanyRequest.getStartYear() );
        }
        if ( updateCompanyRequest.getTaxNumber() != null ) {
            existingCompany.setTaxNumber( updateCompanyRequest.getTaxNumber() );
        }
        if ( updateCompanyRequest.getTotalRoomNumber() != null ) {
            existingCompany.setTotalRoomNumber( updateCompanyRequest.getTotalRoomNumber() );
        }
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
