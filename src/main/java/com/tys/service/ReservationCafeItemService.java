package com.tys.service;

import com.tys.dto.ReservationCafeItemDto;
import com.tys.mapper.ReservationCafeItemMapper;
import com.tys.model.Cafe;
import com.tys.model.Reservation;
import com.tys.model.ReservationCafeItem;
import com.tys.repository.CafeRepository;
import com.tys.repository.ReservationCafeItemRepository;
import com.tys.repository.ReservationRepository;
import com.tys.request.UpdateReservationCafeItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationCafeItemService {

    private final ReservationRepository reservationRepository;
    private final CafeRepository cafeRepository;
    private final ReservationCafeItemRepository reservationCafeItemRepository;
    private final ReservationCafeItemMapper mapper;

    @Transactional
    public ReservationCafeItemDto addCafeItemToReservation(ReservationCafeItemDto dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId())
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        Cafe cafe = cafeRepository.findById(dto.getCafeId())
                .orElseThrow(() -> new RuntimeException("Cafe ürünü bulunamadı"));

        int addCount = dto.getCount() != null ? dto.getCount() : 0;
        if (addCount <= 0) {
            throw new IllegalArgumentException("Cafe adedi 0'dan büyük olmalıdır.");
        }

        // Aynı ürün rezervasyonda varsa adedi artır; yoksa yeni satır ekle
        ReservationCafeItem item = reservationCafeItemRepository
                .findByReservation_IdAndCafe_Id(dto.getReservationId(), dto.getCafeId())
                .map(existing -> {
                    int current = existing.getCount() != null ? existing.getCount() : 0;
                    existing.setCount(current + addCount);
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(dto, reservation, cafe));

        ReservationCafeItem saved = reservationCafeItemRepository.save(item);
        return mapper.toDto(saved);
    }

    /**
     * Hesap ekranından tek bir cafe kaleminin adedini günceller.
     * count &lt;= 0 ise kalem silinir (null döner).
     */
    @Transactional
    public ReservationCafeItemDto updateCafeItemCount(UpdateReservationCafeItemRequest request) {
        ReservationCafeItem item = findItemForUpdate(request);

        Integer newCount = request.getCount();
        if (newCount == null || newCount <= 0) {
            reservationCafeItemRepository.delete(item);
            return null;
        }

        item.setCount(newCount);
        return mapper.toDto(reservationCafeItemRepository.save(item));
    }

    /**
     * Hesap ekranındaki Güncelle butonu: rezervasyona ait birden fazla cafe adedini bir kerede günceller.
     */
    @Transactional
    public List<ReservationCafeItemDto> updateCafeItemCounts(Long reservationId, List<UpdateReservationCafeItemRequest> requests) {
        if (!reservationRepository.existsById(reservationId)) {
            throw new RuntimeException("Rezervasyon bulunamadı");
        }
        if (requests == null || requests.isEmpty()) {
            return getReservationCafeItemDetailsByReservationId(reservationId);
        }

        for (UpdateReservationCafeItemRequest request : requests) {
            request.setReservationId(reservationId);
            updateCafeItemCount(request);
        }
        return getReservationCafeItemDetailsByReservationId(reservationId);
    }

    private ReservationCafeItem findItemForUpdate(UpdateReservationCafeItemRequest request) {
        if (request.getId() != null) {
            return reservationCafeItemRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Cafe kalemi bulunamadı: " + request.getId()));
        }
        if (request.getReservationId() == null || request.getCafeId() == null) {
            throw new IllegalArgumentException("Güncelleme için id veya reservationId + cafeId zorunludur.");
        }
        return reservationCafeItemRepository
                .findByReservation_IdAndCafe_Id(request.getReservationId(), request.getCafeId())
                .orElseThrow(() -> new RuntimeException(
                        "Bu rezervasyonda cafe ürünü bulunamadı. reservationId="
                                + request.getReservationId() + ", cafeId=" + request.getCafeId()));
    }

    @Transactional(readOnly = true)
    public BigDecimal getReservationCafeAmountByReservationId(Long reservationId) {
        return getReservationCafeItemDetailsByReservationId(reservationId).stream()
                .map(ReservationCafeItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Ürün adına göre adetleri toplar (aynı ürün birden fazla satırdaysa ezilmez).
     */
    @Transactional(readOnly = true)
    public Map<String, Integer> getReservationCafeItemListByReservationId(Long reservationId) {
        Map<String, Integer> cafeCountMap = new HashMap<>();

        for (ReservationCafeItemDto item : getReservationCafeItemDetailsByReservationId(reservationId)) {
            if (item.getCafeName() == null || item.getCafeName().isBlank()) {
                continue;
            }
            int count = item.getCount() != null ? item.getCount() : 0;
            cafeCountMap.merge(item.getCafeName(), count, Integer::sum);
        }

        return cafeCountMap;
    }

    @Transactional(readOnly = true)
    public List<ReservationCafeItemDto> getReservationCafeItemDetailsByReservationId(Long reservationId) {
        return reservationCafeItemRepository.findAllByReservation_Id(reservationId).stream()
                .map(mapper::toDto)
                .toList();
    }
}
