package com.tys.service;

import com.tys.domain.company.CompanyRoomCountRule;
import com.tys.domain.company.CompanyTssCodeUniquenessRule;
import com.tys.domain.company.CompanyUsernameUniquenessRule;
import com.tys.dto.CompanyDto;
import com.tys.mapper.CompanyMapper;
import com.tys.model.Company;
import com.tys.repository.CompanyRepository;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.LoginRequest;
import com.tys.request.UpdateCompanyRequest;
import com.tys.response.LoginResponse;
import com.tys.security.JwtService;
import com.tys.security.PasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final JwtService jwtService;
    private final PasswordService passwordService;
    private final CompanyUsernameUniquenessRule companyUsernameUniquenessRule;
    private final CompanyTssCodeUniquenessRule companyTssCodeUniquenessRule;
    private final CompanyRoomCountRule companyRoomCountRule;

    public void createCompany(CreateCompanyRequest request) {
        companyUsernameUniquenessRule.assertAvailable(request.getUsername(), null);
        companyTssCodeUniquenessRule.assertAvailable(request.getFacilityNumber(), null);
        Company company = companyMapper.createCompanyRequestToEntity(request);
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new RuntimeException("Şifre gerekli.");
        }
        company.setPassword(passwordService.hash(request.getPassword()));
        companyRepository.save(company);
    }

    public void deleteCompany(DeleteCompanyRequest request) {
        if (!companyRepository.existsById(request.getId())) {
            throw new RuntimeException("Company not found with Id: " + request.getId());
        }
        companyRepository.deleteById(request.getId());
    }

    public CompanyDto getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with Id: " + id));
        return companyMapper.toDto(company);
    }

    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(companyMapper::toDto)
                .toList();
    }

    public List<String> getCompanyNames() {
        return companyRepository.findAllCompanyNames();
    }

    public void updateCompany(UpdateCompanyRequest request) {
        Company existingCompany = companyRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Company not found with Id: " + request.getId()));
        companyRoomCountRule.assertTotalRoomNumberNotBelowRegistered(existingCompany, request.getTotalRoomNumber());
        companyTssCodeUniquenessRule.assertAvailable(request.getFacilityNumber(), request.getId());
        companyMapper.updateExistingCompanyWithCompanyRequest(request, existingCompany);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existingCompany.setPassword(passwordService.hash(request.getPassword()));
        }
        companyRepository.save(existingCompany);
    }

    public LoginResponse login(LoginRequest request, Boolean isAdmin) {
        // Güvenlik için kullanıcı adı veya şifre hatalı mesajı kullan
        String errorMessage = "Kullanıcı adı veya şifre hatalı.";

        Optional<Company> optionalCompany = companyRepository.findByUsername(request.getUsername());

        if (optionalCompany.isEmpty()) {
            return new LoginResponse(false, errorMessage, null);
        }

        Company company = optionalCompany.get();

        String companyPassword = company.getPassword();
        if (!passwordService.matches(request.getPassword(), companyPassword)) {
            return new LoginResponse(false, errorMessage, null);
        }
        if (!passwordService.isHashed(companyPassword)) {
            company.setPassword(passwordService.hash(request.getPassword()));
            companyRepository.save(company);
        }

        // JWT oluştur (companyId claim ile)
        String token = jwtService.generateToken(company.getId());

        return new LoginResponse(true, "Giriş başarılı.", token);
    }

}
