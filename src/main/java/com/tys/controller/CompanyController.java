package com.tys.controller;

import com.tys.model.Company;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies")
public class CompanyController {
    private CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<Void> createCompany(@RequestBody CreateCompanyRequest request) {
        companyService.saveCompany(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{request}")
    public ResponseEntity<Void> deleteCompany(@ModelAttribute DeleteCompanyRequest request) {
        companyService.deleteCompany(request);
        return ResponseEntity.ok().build();
    }


  /*  // GET mapping to retrieve a car by its ID
    @GetMapping("/{id}")
    public Company getCompany(@PathVariable GetCompanyRequest request) {
        Company company = companyService.get(request);
        if (company == null) {
            throw new ("Company with id " + id + " not found");
        }
        return company;
    }   */


}
