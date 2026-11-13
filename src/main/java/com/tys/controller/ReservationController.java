package com.tys.controller;

import com.tys.dto.GuestDto;
import com.tys.dto.ReservationDto;
import com.tys.request.CreateReservationRequest;
import com.tys.request.DeleteReservationRequest;
import com.tys.request.CreatePaidAmountRequest;
import com.tys.request.EarlyGuestCheckOutRequest;
import com.tys.request.UpdateReservationRequest;
import com.tys.response.EarlyCheckOutOutcome;
import com.tys.security.CurrentActor;
import com.tys.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final CurrentActor currentActor;

    private Long getCurrentCompanyIdFromAuth() {
        return currentActor.requireCompanyId();
    }
    @PostMapping("/create")
    public ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        try {
            Long companyId = getCurrentCompanyIdFromAuth();
            reservationService.createReservation(request, companyId);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> updateReservation(@RequestBody UpdateReservationRequest request) {
        try {
            Long companyId = getCurrentCompanyIdFromAuth();
            String kbsWarning = reservationService.updateReservation(request, companyId);
            if (kbsWarning != null && !kbsWarning.isBlank()) {
                return ResponseEntity.ok(Map.of("message", kbsWarning));
            }
            return ResponseEntity.ok(Map.of());
        } catch (ResponseStatusException ex) {
            throw ex;
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
        reservationService.deleteReservation(request, getCurrentCompanyIdFromAuth());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservationByIdd(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id, getCurrentCompanyIdFromAuth()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
            Long companyId = getCurrentCompanyIdFromAuth();
            return ResponseEntity.ok(reservationService.getAllWithGuests(companyId));
    }

    @PostMapping("/{reservationId}/guests/{guestId}/early-check-out")
    public ResponseEntity<Map<String, Object>> earlyCheckOutGuest(
            @PathVariable("reservationId") Long reservationId,
            @PathVariable("guestId") Long guestId,
            @RequestBody(required = false) EarlyGuestCheckOutRequest request) {
        try {
            EarlyCheckOutOutcome outcome = reservationService.earlyCheckOutGuest(
                    reservationId, guestId, request, getCurrentCompanyIdFromAuth());
            String message = outcome.isReservationClosed()
                    ? "Son misafir de çıkış yaptı. Rezervasyon check-out oldu."
                    : "Misafirin erken check-out işlemi tamamlandı. Diğer misafirler konaklamaya devam ediyor.";
            if (outcome.getKbsWarning() != null && !outcome.getKbsWarning().isBlank()) {
                message = message + " " + outcome.getKbsWarning();
            }
            return ResponseEntity.ok(Map.of("message", message, "reservationClosed", outcome.isReservationClosed()));
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<?> addPayment(
            @PathVariable("id") Long id,
            @RequestBody CreatePaidAmountRequest request
    ) {
        reservationService.addPayment(id, request, getCurrentCompanyIdFromAuth());
        return ResponseEntity.ok().build();
    }

}
