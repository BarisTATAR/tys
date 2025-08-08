package com.tys.controller;

import com.tys.dto.ReservationDto;
import com.tys.model.Reservation;
import com.tys.request.CreateReservationRequest;
import com.tys.request.DeleteReservationRequest;
import com.tys.request.UpdateReservationRequest;
import com.tys.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/create")
    public ResponseEntity<Void> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        reservationService.createReservation(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateReservation(@RequestBody UpdateReservationRequest request) {
        reservationService.updateReservation(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteReservation(@RequestBody DeleteReservationRequest request) {
        reservationService.deleteReservation(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDto> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReservationDto>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

}
