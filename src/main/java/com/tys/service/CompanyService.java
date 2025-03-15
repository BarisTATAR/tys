package com.tys.service;

import com.tys.model.Company;
import com.tys.repository.CompanyRepository;
import com.tys.request.CreateCompanyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public void saveCompany(CreateCompanyRequest request) {
        // mapstruct ile entity ye maple
        /*Company company = mapper.requestToEntity(request);
        return companyRepository.save(company);*/
    }

    public void deleteCompany(Long companyId) {
        companyRepository.deleteById(companyId);
    }

}
