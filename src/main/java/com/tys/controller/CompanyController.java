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
import com.tys.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final SessionUtil sessionUtil;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = companyService.login(request, false);

        if (response.isSuccess() && response.getToken() != null) {
            // Session'a token kaydet
            sessionUtil.setTokenToSession(session, response.getToken());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin_login")
    public ResponseEntity<LoginResponse> adminLogin(@RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = companyService.login(request, true);

        if (response.isSuccess() && response.getToken() != null) {
            // Session'a token kaydet
            sessionUtil.setTokenToSession(session, response.getToken());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        sessionUtil.clearSession(session);
        Map<String, String> response = new java.util.HashMap<>();
        response.put("message", "Başarıyla çıkış yapıldı.");
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
     * Session'dan token'ı kontrol eder ve companyId döner
     */
    @GetMapping("/current-company-id")
    public ResponseEntity<?> getCurrentCompanyId(HttpSession session) {
        if (!sessionUtil.isSessionValid(session)) {
            Map<String, String> error = new java.util.HashMap<>();
            error.put("message", "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.");
            error.put("error", "SESSION_EXPIRED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Session'ı yenile (her istekte timeout süresini sıfırla)
        sessionUtil.touchSession(session);

        Long companyId = sessionUtil.getCompanyIdFromSession(session);
        if (companyId != null) {
            return ResponseEntity.ok(companyId);
        }

        Map<String, String> error = new java.util.HashMap<>();
        error.put("message", "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.");
        error.put("error", "SESSION_EXPIRED");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @PostMapping("/upload-rules-file")
    public ResponseEntity<?> uploadWord(HttpSession session,
                                        @RequestParam("file") MultipartFile file) {
        try {
            // Session geçerliliğini kontrol et
            if (!sessionUtil.isSessionValid(session)) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.");
                error.put("error", "SESSION_EXPIRED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Session'ı yenile (her istekte timeout süresini sıfırla)
            sessionUtil.touchSession(session);

            // Session'dan companyId'yi al
            Long companyId = sessionUtil.getCompanyIdFromSession(session);

            if (companyId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.");
                error.put("error", "SESSION_EXPIRED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

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
    public ResponseEntity<?> getRulesFile(HttpSession session) {
        if (!sessionUtil.isSessionValid(session)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.");
            error.put("error", "SESSION_EXPIRED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        sessionUtil.touchSession(session);

        Long companyId = sessionUtil.getCompanyIdFromSession(session);
        if (companyId == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Oturum süreniz dolmuş. Lütfen tekrar giriş yapın.");
            error.put("error", "SESSION_EXPIRED");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<RulesFileDto> dto = rulesFileService.getByCompanyId(companyId);
        if (dto.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Bu şirket için kurallar dosyası bulunamadı.");
            error.put("error", "NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(dto.get());
    }
}