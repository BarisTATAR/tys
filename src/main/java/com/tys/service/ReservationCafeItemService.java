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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rezervasyon kafe hesabı. Dışarı açık tüm işlemler oturumdaki işletmeyle (companyId) sınırlıdır:
 * rezervasyon ve kafe ürünü aynı işletmeye ait olmalıdır.
 */
@Service
@RequiredArgsConstructor
public class ReservationCafeItemService {

    private final ReservationRepository reservationRepository;
    private final CafeRepository cafeRepository;
    private final ReservationCafeItemRepository reservationCafeItemRepository;
    private final ReservationCafeItemMapper mapper;

    @Transactional
    public ReservationCafeItemDto addCafeItemToReservation(ReservationCafeItemDto dto, Long companyId) {
        if (dto.getReservationId() == null || dto.getCafeId() == null) {
            throw new IllegalArgumentException("Rezervasyon ve cafe ürünü seçilmelidir.");
        }
        Reservation reservation = requireReservationOfCompany(dto.getReservationId(), companyId);
        assertBillOpen(reservation);

        Cafe cafe = cafeRepository.findById(dto.getCafeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe ürünü bulunamadı."));
        if (!companyId.equals(cafe.getCompanyId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
        }
        if (Boolean.FALSE.equals(cafe.getActive())) {
            throw new IllegalArgumentException("Bu ürün menüden kaldırılmış.");
        }

        int addCount = dto.getCount() != null ? dto.getCount() : 0;
        if (addCount <= 0) {
            throw new IllegalArgumentException("Cafe adedi 0'dan büyük olmalıdır.");
        }
        if (addCount > 999) {
            throw new IllegalArgumentException("Cafe adedi en fazla 999 olabilir.");
        }

        // Aynı ürün aynı fiyatla rezervasyonda varsa adedi artır; fiyat değişmişse yeni satır (eski satır eski fiyatla kalır)
        ReservationCafeItem item = reservationCafeItemRepository
                .findFirstByReservation_IdAndCafe_IdAndUnitPrice(dto.getReservationId(), dto.getCafeId(), cafe.getPrice())
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
    public ReservationCafeItemDto updateCafeItemCount(UpdateReservationCafeItemRequest request, Long companyId) {
        ReservationCafeItem item = findItemForUpdate(request);
        assertItemOfCompany(item, companyId);
        assertBillOpen(item.getReservation());
        return applyCount(item, request.getCount());
    }

    /** Check-out yapılmış veya iptal edilmiş rezervasyonun kafe hesabı değiştirilemez (borç hesabı kapanmıştır). */
    private void assertBillOpen(Reservation reservation) {
        if (reservation == null) {
            return;
        }
        com.tys.enums.ReservationStatus status = reservation.getReservationStatus();
        if (status == com.tys.enums.ReservationStatus.CHECK_OUT_DONE) {
            throw new IllegalArgumentException("Check-out yapılmış rezervasyonun kafe hesabı değiştirilemez.");
        }
        if (status == com.tys.enums.ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("İptal edilmiş rezervasyonun kafe hesabı değiştirilemez.");
        }
    }

    /**
     * Hesap ekranındaki Güncelle butonu: rezervasyona ait birden fazla cafe adedini bir kerede günceller.
     */
    @Transactional
    public List<ReservationCafeItemDto> updateCafeItemCounts(Long reservationId, List<UpdateReservationCafeItemRequest> requests, Long companyId) {
        Reservation reservation = requireReservationOfCompany(reservationId, companyId);
        if (requests == null || requests.isEmpty()) {
            return getReservationCafeItemDetailsByReservationId(reservationId);
        }
        assertBillOpen(reservation);

        for (UpdateReservationCafeItemRequest request : requests) {
            request.setReservationId(reservationId);
            ReservationCafeItem item = findItemForUpdate(request);
            // Kalem id ile bulunduysa bile yoldaki rezervasyona ait olmalı
            if (item.getReservation() == null || !reservationId.equals(item.getReservation().getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
            }
            applyCount(item, request.getCount());
        }
        return getReservationCafeItemDetailsByReservationId(reservationId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getReservationCafeAmount(Long reservationId, Long companyId) {
        requireReservationOfCompany(reservationId, companyId);
        return getReservationCafeAmountByReservationId(reservationId);
    }

    @Transactional(readOnly = true)
    public Map<String, Integer> getReservationCafeItemList(Long reservationId, Long companyId) {
        requireReservationOfCompany(reservationId, companyId);
        return getReservationCafeItemListByReservationId(reservationId);
    }

    @Transactional(readOnly = true)
    public List<ReservationCafeItemDto> getReservationCafeItemDetails(Long reservationId, Long companyId) {
        requireReservationOfCompany(reservationId, companyId);
        return getReservationCafeItemDetailsByReservationId(reservationId);
    }

    /**
     * Yalnız sunucu içi kullanım (check-out borç hesabı); yetki kontrolü çağıran tarafta yapılmıştır.
     */
    @Transactional(readOnly = true)
    public BigDecimal getReservationCafeAmountByReservationId(Long reservationId) {
        return getReservationCafeItemDetailsByReservationId(reservationId).stream()
                .map(ReservationCafeItemDto::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ReservationCafeItemDto applyCount(ReservationCafeItem item, Integer newCount) {
        if (newCount == null || newCount <= 0) {
            reservationCafeItemRepository.delete(item);
            return null;
        }
        if (newCount > 999) {
            throw new IllegalArgumentException("Cafe adedi en fazla 999 olabilir.");
        }
        item.setCount(newCount);
        return mapper.toDto(reservationCafeItemRepository.save(item));
    }

    private Reservation requireReservationOfCompany(Long reservationId, Long companyId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("Rezervasyon seçilmelidir.");
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı."));
        if (companyId == null || reservation.getCompany() == null
                || !companyId.equals(reservation.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
        }
        return reservation;
    }

    private void assertItemOfCompany(ReservationCafeItem item, Long companyId) {
        Reservation reservation = item.getReservation();
        if (companyId == null || reservation == null || reservation.getCompany() == null
                || !companyId.equals(reservation.getCompany().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bu kayda erişim yok.");
        }
    }

    private ReservationCafeItem findItemForUpdate(UpdateReservationCafeItemRequest request) {
        if (request.getId() != null) {
            return reservationCafeItemRepository.findById(request.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cafe kalemi bulunamadı."));
        }
        if (request.getReservationId() == null || request.getCafeId() == null) {
            throw new IllegalArgumentException("Güncelleme için id veya reservationId + cafeId zorunludur.");
        }
        return reservationCafeItemRepository
                .findFirstByReservation_IdAndCafe_IdOrderByIdDesc(request.getReservationId(), request.getCafeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Bu rezervasyonda cafe ürünü bulunamadı."));
    }

    private Map<String, Integer> getReservationCafeItemListByReservationId(Long reservationId) {
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

    private List<ReservationCafeItemDto> getReservationCafeItemDetailsByReservationId(Long reservationId) {
        return reservationCafeItemRepository.findAllByReservation_Id(reservationId).stream()
                .map(mapper::toDto)
                .toList();
    }
}
