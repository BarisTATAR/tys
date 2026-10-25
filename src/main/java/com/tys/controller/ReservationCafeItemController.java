package com.tys.controller;

import com.tys.dto.ReservationCafeItemDto;
import com.tys.request.UpdateReservationCafeItemRequest;
import com.tys.service.ReservationCafeItemService;
import lombok.RequiredArgsConstructor;
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
        return ResponseEntity.ok(reservationCafeItemService.addCafeItemToReservation(dto));
    }

    /**
     * Tek cafe kalemi adedini günceller (hesap ekranı).
     * count &lt;= 0 ise kalem silinir.
     */
    @PutMapping("/update")
    public ResponseEntity<ReservationCafeItemDto> updateCafeItem(@RequestBody UpdateReservationCafeItemRequest request) {
        return ResponseEntity.ok(reservationCafeItemService.updateCafeItemCount(request));
    }

    /**
     * Rezervasyona ait birden fazla cafe adedini toplu günceller (Güncelle butonu).
     */
    @PutMapping("/update-batch/{reservationId}")
    public ResponseEntity<List<ReservationCafeItemDto>> updateCafeItemsBatch(
            @PathVariable("reservationId") Long reservationId,
            @RequestBody List<UpdateReservationCafeItemRequest> requests) {
        return ResponseEntity.ok(reservationCafeItemService.updateCafeItemCounts(reservationId, requests));
    }

    @GetMapping("/list/{reservationId}")
    public ResponseEntity<Map<String, Integer>> getReservationCafeItemListByReservationId(
            @PathVariable("reservationId") Long reservationId) {
        return ResponseEntity.ok(reservationCafeItemService.getReservationCafeItemListByReservationId(reservationId));
    }

    @GetMapping("/details/{reservationId}")
    public ResponseEntity<List<ReservationCafeItemDto>> getReservationCafeItemDetails(
            @PathVariable("reservationId") Long reservationId) {
        return ResponseEntity.ok(reservationCafeItemService.getReservationCafeItemDetailsByReservationId(reservationId));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<BigDecimal> getReservationCafeItemByReservationId(
            @PathVariable("reservationId") Long reservationId) {
        return ResponseEntity.ok(reservationCafeItemService.getReservationCafeAmountByReservationId(reservationId));
    }
}
