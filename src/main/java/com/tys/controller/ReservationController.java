package com.tys.controller;

import com.tys.dto.GuestDto;
import com.tys.dto.ReservationDto;
import com.tys.request.CreateReservationRequest;
import com.tys.request.DeleteReservationRequest;
import com.tys.request.CreatePaidAmountRequest;
import com.tys.request.UpdateReservationRequest;
import com.tys.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationController {

    private final ReservationService reservationService;
    private Long getCurrentCompanyIdFromAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            return (Long) auth.getPrincipal();
        }
        throw new IllegalStateException("Kimlik doğrulanamadı.");
    }
    @PostMapping("/create")
    public ResponseEntity<Void> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        Long companyId = getCurrentCompanyIdFromAuth();
        reservationService.createReservation(request, companyId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateReservation(@RequestBody UpdateReservationRequest request) {
        try {
            reservationService.updateReservation(request);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            // Business rule hatasını yakala ve mesaj olarak döndür
            return ResponseEntity
                    .badRequest() // 400 Bad Request
                    .body(Map.of("message", ex.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteReservation(@RequestBody DeleteReservationRequest request) {
        reservationService.deleteReservation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservationByIdd(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
            Long companyId = getCurrentCompanyIdFromAuth();
            return ResponseEntity.ok(reservationService.getAllWithGuests(companyId));
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<?> addPayment(
            @PathVariable("id") Long id,
            @RequestBody CreatePaidAmountRequest request
    ) {
        reservationService.addPayment(id, request);
        return ResponseEntity.ok().build();
    }

}
