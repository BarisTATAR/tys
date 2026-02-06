package com.tys.controller;

import com.tys.dto.CompanyDto;
import com.tys.dto.RulesFileDto;
import com.tys.model.Company;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.LoginRequest;
import com.tys.request.UpdateCompanyRequest;
import com.tys.response.LoginResponse;
import com.tys.service.CompanyService;
import com.tys.service.RulesFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/company")
@CrossOrigin(origins = "http://localhost:3000")
public class CompanyController {

    private final CompanyService companyService;
    private final RulesFileService rulesFileService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = companyService.login(request, false);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin_login")
    public ResponseEntity<LoginResponse> adminLogin(@RequestBody LoginRequest request) {
        LoginResponse response = companyService.login(request, true);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Başarıyla çıkış yapıldı. Token'ı client tarafında silin.");
        return ResponseEntity.ok(response);
    }


    @PostMapping("/create")
    public ResponseEntity<Void> createCompany(@RequestBody CreateCompanyRequest request) {
        companyService.createCompany(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCompany(@RequestBody DeleteCompanyRequest request) {
        companyService.deleteCompany(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/names")
    public ResponseEntity<List<String>> getCompanyNames() {
        return ResponseEntity.ok(companyService.getCompanyNames());
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateCompany(@RequestBody UpdateCompanyRequest request) {
        companyService.updateCompany(request);
        return ResponseEntity.ok().build();
    }

    /**
     * JWT'den companyId döner (Authorization: Bearer &lt;token&gt; gerekli)
     */
    @GetMapping("/current-company-id")
    public ResponseEntity<Long> getCurrentCompanyId() {
        Long companyId = getCurrentCompanyIdFromAuth();
        return ResponseEntity.ok(companyId);
    }

    @PostMapping("/upload-rules-file")
    public ResponseEntity<?> uploadWord(@RequestParam("file") MultipartFile file) {
        try {
            Long companyId = getCurrentCompanyIdFromAuth();
            RulesFileDto dto = rulesFileService.uploadWord(companyId, file);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "Dosya yükleme hatası: " + e.getMessage());
            error.put("error", "UPLOAD_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/get-rules-file")
    public ResponseEntity<?> getRulesFile() {
        Long companyId = getCurrentCompanyIdFromAuth();
        Optional<RulesFileDto> dto = rulesFileService.getByCompanyId(companyId);
        if (dto.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Bu şirket için kurallar dosyası bulunamadı.");
            error.put("error", "NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(dto.get());
    }

    private Long getCurrentCompanyIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new IllegalStateException("Kimlik doğrulanamadı.");
    }
}