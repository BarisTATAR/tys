package com.tys.controller;
import com.tys.model.Company;
import com.tys.model.Reservation;
import com.tys.model.Room;
import com.tys.request.*;
import com.tys.service.ReservationService;
import com.tys.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/create")
    public ResponseEntity<Void> createReservation(@RequestBody CreateReservationRequest request) {
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
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getReservationById(id));
    }
    @GetMapping("/alls")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }


}
