package com.tys.service;

import com.tys.mapper.CompanyMapper;
import com.tys.model.Company;
import com.tys.repository.CompanyRepository;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.UpdateCompanyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;

    public void createCompany(CreateCompanyRequest request) {
        Company company = companyMapper.createCompanyRequestToEntity(request);
        companyRepository.save(company);
    }

    public void deleteCompany(DeleteCompanyRequest request) {
        if (!companyRepository.existsById(request.getId())) {
            throw new RuntimeException("Company not found with Id: " + request.getId());
        }
        companyRepository.deleteById(request.getId());
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id).orElseThrow(() -> new RuntimeException("Company not found with Id: " + id));
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public void updateCompany(UpdateCompanyRequest request) {
        Company existingCompany = companyRepository.findById(request.getId()).orElseThrow(() -> new RuntimeException("Company not found with Id: " + request.getId()));
        companyMapper.updateExistingCompanyWithCompanyRequest(request, existingCompany);
        companyRepository.save(existingCompany);
    }
}