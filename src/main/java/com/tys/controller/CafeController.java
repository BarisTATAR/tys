package com.tys.controller;

import com.tys.dto.CafeDto;
import com.tys.model.Cafe;
import com.tys.request.*;
import com.tys.service.CafeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cafe")
@CrossOrigin(origins = "http://localhost:3000")
public class CafeController {

    private final CafeService cafeService;

    private Long getCurrentCompanyIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new IllegalStateException("Kimlik doğrulanamadı.");
    }

    @PostMapping("/create")
    public ResponseEntity<Void> createCafe(@RequestBody CreateCafeRequest request) {
        request.setCompanyId(getCurrentCompanyIdFromAuth());
        cafeService.createCafe(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteCafe(@RequestBody DeleteCafeRequest request) {
        Long companyId = getCurrentCompanyIdFromAuth();
        Cafe cafe = cafeService.getCafeById(request.getId());
        if (!cafe.getCompanyId().equals(companyId)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Bu işlem için yetkiniz yok.");
            error.put("error", "FORBIDDEN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        cafeService.deleteCafe(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateCafe(@RequestBody UpdateCafeRequest request) {
        Long companyId = getCurrentCompanyIdFromAuth();
        Cafe cafe = cafeService.getCafeById(request.getId());
        if (!cafe.getCompanyId().equals(companyId)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Bu işlem için yetkiniz yok.");
            error.put("error", "FORBIDDEN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        cafeService.updateCafe(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCafeById(@PathVariable Long id) {
        Long companyId = getCurrentCompanyIdFromAuth();
        Cafe cafe = cafeService.getCafeById(id);
        if (!cafe.getCompanyId().equals(companyId)) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Bu işlem için yetkiniz yok.");
            error.put("error", "FORBIDDEN");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        return ResponseEntity.ok(cafe);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CafeDto>> getAllCafeItems() {
        Long companyId = getCurrentCompanyIdFromAuth();
        return ResponseEntity.ok(cafeService.getAllCafeItems(companyId));
    }
}
