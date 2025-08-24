package com.tys.controller;

import com.tys.dto.ReservationCafeItemDto;
import com.tys.service.ReservationCafeItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservation-cafe-items")
@CrossOrigin(origins = "http://localhost:3000")
public class ReservationCafeItemController {

    private final ReservationCafeItemService reservationCafeItemService;

    @PostMapping("/add")
    public ResponseEntity<ReservationCafeItemDto> addCafeItem(@RequestBody ReservationCafeItemDto dto) {
        reservationCafeItemService.addCafeItemToReservation(dto);
        return ResponseEntity.ok().build();
    }
}
