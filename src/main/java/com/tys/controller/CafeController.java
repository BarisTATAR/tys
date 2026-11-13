package com.tys.controller;

import com.tys.dto.CafeDto;
import com.tys.model.Cafe;
import com.tys.request.*;
import com.tys.security.CurrentActor;
import com.tys.service.CafeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cafe")
public class CafeController {

    private final CafeService cafeService;
    private final CurrentActor currentActor;

    private Long getCurrentCompanyIdFromAuth() {
        return currentActor.requireCompanyId();
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
        request.setCompanyId(companyId);
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
