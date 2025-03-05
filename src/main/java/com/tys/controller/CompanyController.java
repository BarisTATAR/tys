package com.tys.controller;

import com.tys.model.Company;
import com.tys.service.CompanyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/companies")
public class CompanyController {
    private CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }
@PostMapping
    public Company createCompany(@RequestBody Company company){

    return companyService.saveCompany(company);

    }
@DeleteMapping("/{id}")
    public void deleteCompany(@PathVariable Long id) {

        companyService.deleteCompany(id);
    }



}
