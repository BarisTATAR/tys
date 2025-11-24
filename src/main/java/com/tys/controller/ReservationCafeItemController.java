package com.tys.controller;

import com.tys.dto.GuestDto;
import com.tys.dto.ReservationCafeItemDto;
import com.tys.model.ReservationCafeItem;
import com.tys.service.ReservationCafeItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/{reservationId}")
    public ResponseEntity<BigDecimal> getReservationCafeItemByReservationId(@PathVariable("reservationId") Long reservationId) {
        return ResponseEntity.ok(reservationCafeItemService.getReservationCafeAmountByReservationId(reservationId));
    }

    @GetMapping("list/{reservationId}")
    public ResponseEntity<Map<String, Integer>> getReservationCafeItemListByReservationId(@PathVariable("reservationId") Long reservationId) {
        return ResponseEntity.ok(reservationCafeItemService.getReservationCafeItemListByReservationId(reservationId));
    }
}
