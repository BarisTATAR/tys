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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public BigDecimal getReservationCafeAmountByReservationId(Long reservationId) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<ReservationCafeItem> items = reservationCafeItemRepository.findAllByReservationId(reservationId);

        if (items.isEmpty()) {
            throw new RuntimeException("Rezervasyon bulunamadı");
        }

        for (ReservationCafeItem item : items) {
            // Cafe kaydını çek
            Optional<Cafe> cafeOptional = cafeRepository.findById(item.getCafe().getId());

            if (cafeOptional.isPresent()) {
                Cafe cafe = cafeOptional.get();

                // count ve price BigDecimal olarak hazırlanır
                BigDecimal count = BigDecimal.valueOf(item.getCount());
                BigDecimal price = cafe.getPrice(); // BigDecimal türünde olmalı

                // item toplamı = price * count
                BigDecimal itemTotal = price.multiply(count);

                // totalAmount üzerine ekleme (kümülatif toplama)
                totalAmount = totalAmount.add(itemTotal);

            } else {
                throw new RuntimeException("Cafe bulunamadı: " + item.getId());
            }
        }
        //hesap yap
        return totalAmount;

    }

    public Map<String, Integer> getReservationCafeItemListByReservationId(Long reservationId) {
        List<ReservationCafeItem> items = reservationCafeItemRepository.findAllByReservationId(reservationId);

        if (items.isEmpty()) {
            throw new RuntimeException("Rezervasyon bulunamadı");
        }

        Map<String, Integer> cafeCountMap = new HashMap<>();
        for (ReservationCafeItem cafeItem: items) {
            cafeCountMap.put(cafeItem.getCafe().getName(), cafeItem.getCount());
        }

        return  cafeCountMap;
    }
}