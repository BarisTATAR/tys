package com.tys.controller;

import com.tys.dto.CafeDto;
import com.tys.dto.GuestDto;
import com.tys.model.Cafe;
import com.tys.model.Guest;
import com.tys.request.*;
import com.tys.service.CafeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cafe")
@CrossOrigin(origins = "http://localhost:3000")
public class CafeController {
    private final CafeService cafeService;
    @PostMapping("/create")
    public ResponseEntity<Void> createCafe(@RequestBody CreateCafeRequest request) {
        cafeService.createCafe(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCafe(@RequestBody DeleteCafeRequest request) {
        cafeService.deleteCafe(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateCafe(@RequestBody UpdateCafeRequest request) {
        cafeService.updateCafe(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cafe> getCafeById(@PathVariable Long id) {
        return ResponseEntity.ok(cafeService.getCafeById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CafeDto>> getAllCafeItems() {
        return ResponseEntity.ok(cafeService.getAllCafeItems());
    }


}
