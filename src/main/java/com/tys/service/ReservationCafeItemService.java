package com.tys.service;

import com.tys.dto.ReservationCafeItemDto;
import com.tys.mapper.ReservationCafeItemMapper;
import com.tys.model.Cafe;
import com.tys.model.Reservation;
import com.tys.model.ReservationCafeItem;
import com.tys.repository.CafeRepository;
import com.tys.repository.ReservationCafeItemRepository;
import com.tys.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationCafeItemService {
    private final ReservationRepository reservationRepository;
    private final CafeRepository cafeRepository;
    private final ReservationCafeItemRepository reservationCafeItemRepository;
    private final ReservationCafeItemMapper mapper;

    public ReservationCafeItemDto addCafeItemToReservation(ReservationCafeItemDto dto) {

        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        Cafe cafe = cafeRepository.findById(dto.getCafeId())
                .orElseThrow(() -> new RuntimeException("Cafe ürünü bulunamadı"));

        // Yeni item oluştur
        ReservationCafeItem item = mapper.toEntity(dto, reservation, cafe);

        // Rezervasyonun cafeItems listesine ekle
        reservation.getCafeItems().add(item);

        // Kalemi kaydet (cascade açıksa rezervasyon kaydında da kayıt olur)
        reservationCafeItemRepository.save(item);

        return mapper.toDto(item);
    }
}