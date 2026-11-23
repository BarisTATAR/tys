package com.tys.controller;

import com.tys.dto.ReservationCafeItemDto;
import com.tys.request.UpdateReservationCafeItemRequest;
import com.tys.security.CurrentActor;
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
public class ReservationCafeItemController {

    private final ReservationCafeItemService reservationCafeItemService;
    private final CurrentActor currentActor;

    @PostMapping("/add")
    public ResponseEntity<ReservationCafeItemDto> addCafeItem(@RequestBody ReservationCafeItemDto dto) {
        return ResponseEntity.ok(reservationCafeItemService.addCafeItemToReservation(dto, currentActor.requireCompanyId()));
    }

    /**
     * Tek cafe kalemi adedini günceller (hesap ekranı).
     * count &lt;= 0 ise kalem silinir.
     */
    @PutMapping("/update")
    public ResponseEntity<ReservationCafeItemDto> updateCafeItem(@RequestBody UpdateReservationCafeItemRequest request) {
        return ResponseEntity.ok(reservationCafeItemService.updateCafeItemCount(request, currentActor.requireCompanyId()));
    }

    /**
     * Rezervasyona ait birden fazla cafe adedini toplu günceller (Güncelle butonu).
     */
    @PutMapping("/update-batch/{reservationId}")
    public ResponseEntity<List<ReservationCafeItemDto>> updateCafeItemsBatch(
            @PathVariable("reservationId") Long reservationId,
            @RequestBody List<UpdateReservationCafeItemRequest> requests) {
        return ResponseEntity.ok(reservationCafeItemService.updateCafeItemCounts(
                reservationId, requests, currentActor.requireCompanyId()));
    }

    @GetMapping("/list/{reservationId}")
    public ResponseEntity<Map<String, Integer>> getReservationCafeItemListByReservationId(
            @PathVariable("reservationId") Long reservationId) {
        return ResponseEntity.ok(reservationCafeItemService.getReservationCafeItemList(
                reservationId, currentActor.requireCompanyId()));
    }

    @GetMapping("/details/{reservationId}")
    public ResponseEntity<List<ReservationCafeItemDto>> getReservationCafeItemDetails(
            @PathVariable("reservationId") Long reservationId) {
        return ResponseEntity.ok(reservationCafeItemService.getReservationCafeItemDetails(
                reservationId, currentActor.requireCompanyId()));
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<BigDecimal> getReservationCafeItemByReservationId(
            @PathVariable("reservationId") Long reservationId) {
        return ResponseEntity.ok(reservationCafeItemService.getReservationCafeAmount(
                reservationId, currentActor.requireCompanyId()));
    }
}
