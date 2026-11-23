package com.tys.controller;

import com.tys.dto.CompanyDto;
import com.tys.dto.RulesFileDto;
import com.tys.request.CreateCompanyRequest;
import com.tys.request.DeleteCompanyRequest;
import com.tys.request.LoginRequest;
import com.tys.request.SendRulesSmsRequest;
import com.tys.request.UpdateCompanyRequest;
import com.tys.response.LoginResponse;
import com.tys.security.AuthCookie;
import com.tys.security.CurrentActor;
import com.tys.security.JwtService;
import com.tys.security.LoginGuard;
import com.tys.security.TokenBlacklist;
import com.tys.service.CompanyService;
import com.tys.service.RulesFileService;
import com.tys.service.RulesSmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;
    private final RulesFileService rulesFileService;
    private final RulesSmsService rulesSmsService;
    private final CurrentActor currentActor;
    private final LoginGuard loginGuard;
    private final TokenBlacklist tokenBlacklist;
    private final JwtService jwtService;
    private final AuthCookie authCookie;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        return respondToLogin(request, false, httpRequest, httpResponse);
    }

    @PostMapping("/admin_login")
    public ResponseEntity<LoginResponse> adminLogin(@RequestBody LoginRequest request,
                                                    HttpServletRequest httpRequest,
                                                    HttpServletResponse httpResponse) {
        return respondToLogin(request, true, httpRequest, httpResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse,
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring("Bearer ".length());
        }
        if (token == null || token.isBlank()) {
            token = AuthCookie.read(httpRequest);
        }
        if (token == null || token.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Oturum bulunamadı."));
        }
        Date expiration = jwtService.getExpiration(token);
        tokenBlacklist.revoke(token, expiration.toInstant());
        authCookie.clear(httpRequest, httpResponse);
        return ResponseEntity.ok(Map.of("message", "Oturum iptal edildi."));
    }


    @PostMapping("/create")
    public ResponseEntity<Void> createCompany(@RequestBody CreateCompanyRequest request) {
        currentActor.requireAdmin();
        companyService.createCompany(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCompany(@RequestBody DeleteCompanyRequest request) {
        currentActor.requireAdmin();
        companyService.deleteCompany(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long id) {
        if (!currentActor.isAdmin()) {
            Long companyId = currentActor.requireCompanyId();
            if (!companyId.equals(id)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
            }
        }
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        currentActor.requireAdmin();
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @GetMapping("/names")
    public ResponseEntity<List<String>> getCompanyNames() {
        currentActor.requireAdmin();
        return ResponseEntity.ok(companyService.getCompanyNames());
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateCompany(@RequestBody UpdateCompanyRequest request) {
        currentActor.requireAdmin();
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

    /**
     * İşletme kurallarını onay talebi ile birlikte verilen telefon numarasına SMS olarak gönderir.
     */
    @PostMapping("/send-rules-sms")
    public ResponseEntity<?> sendRulesSms(@RequestBody SendRulesSmsRequest request) {
        try {
            Long companyId = getCurrentCompanyIdFromAuth();
            return ResponseEntity.ok(rulesSmsService.sendRulesSms(companyId, request));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "VALIDATION_ERROR");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "SMS gönderilemedi: " + e.getMessage());
            error.put("error", "SMS_SEND_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Bir rezervasyona gönderilmiş kurallar onay linklerinin durumunu listeler.
     */
    @GetMapping("/rules-approvals/{reservationId}")
    public ResponseEntity<?> getRulesApprovals(@PathVariable Long reservationId) {
        try {
            Long companyId = getCurrentCompanyIdFromAuth();
            return ResponseEntity.ok(rulesSmsService.getApprovalsByReservation(companyId, reservationId));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "VALIDATION_ERROR");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    private Long getCurrentCompanyIdFromAuth() {
        return currentActor.requireCompanyId();
    }

    private ResponseEntity<LoginResponse> respondToLogin(LoginRequest request,
                                                         boolean adminLogin,
                                                         HttpServletRequest httpRequest,
                                                         HttpServletResponse httpResponse) {
        String username = request != null ? request.getUsername() : null;
        try {
            loginGuard.assertAllowed(username);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .body(new LoginResponse(false, ex.getReason(), null));
        }
        LoginResponse response = companyService.login(request, adminLogin);
        if (response.isSuccess()) {
            loginGuard.recordSuccess(username);
            if (response.getToken() != null && !response.getToken().isBlank()) {
                authCookie.write(httpRequest, httpResponse, response.getToken());
                response.setToken(null);
            }
            return ResponseEntity.ok(response);
        }
        loginGuard.recordFailure(username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}