package com.tys.service;

import com.tys.model.Company;
import com.tys.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company saveCompany(Company company) {

        return companyRepository.save(company);
    }

    public void deleteCompany(Long companyId) {

        companyRepository.deleteById(companyId);
    }

}
