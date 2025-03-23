package com.tys.controller;

import com.tys.model.Company;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.UpdateCompanyRequest;
import com.tys.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<Void> createCompany(@RequestBody CreateCompanyRequest request) {
        companyService.createCompany(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCompany(@RequestBody DeleteCompanyRequest request) {
        companyService.deleteCompany(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @GetMapping
    public ResponseEntity<List<Company>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @PutMapping
    public ResponseEntity<Void> updateCompany(@RequestBody UpdateCompanyRequest request) {
        companyService.updateCompany(request);
        return ResponseEntity.ok().build();
    }
}
