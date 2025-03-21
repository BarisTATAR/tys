package com.tys.service;

import com.tys.model.Company;
import com.tys.repository.CompanyRepository;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.UpdateCompanyRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    private ModelMapper modelMapper;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
        this.modelMapper = new ModelMapper();
    }

    public void saveCompany(CreateCompanyRequest request) {
        Company company = modelMapper.map(request, Company.class);
        companyRepository.save(company);
    }

    public void deleteCompany(DeleteCompanyRequest request) {

        if (!companyRepository.existsById(request.getId())) {

            throw new RuntimeException("Company not found with Id: " + request.getId());

        }
        companyRepository.deleteById(request.getId());
    }

    public Company getCompanyById(Long id){

        return companyRepository.findById(id).orElseThrow();

    }

    public List<Company> getAllCompanies() {

        return companyRepository.findAll();
    }

    public Company updateCompany(UpdateCompanyRequest request) {
        Company company = modelMapper.map(request, Company.class);
        return companyRepository.save(company);
    }
}
