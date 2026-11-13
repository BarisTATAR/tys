package com.tys.mapper;

import com.tys.dto.ReservationCafeItemDto;
import com.tys.model.Cafe;
import com.tys.model.Reservation;
import com.tys.model.ReservationCafeItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Manuel mapper (MapStruct kullanılmıyor — IDE'nin ReservationCafeItemMapperImpl üretmesini engeller).
 */
@Component
public class ReservationCafeItemMapper {

    public ReservationCafeItem toEntity(ReservationCafeItemDto dto, Reservation reservation, Cafe cafe) {
        return ReservationCafeItem.builder()
                .reservation(reservation)
                .cafe(cafe)
                .count(dto.getCount())
                .build();
    }

    public ReservationCafeItemDto toDto(ReservationCafeItem entity) {
        Cafe cafe = entity.getCafe();
        BigDecimal unitPrice = cafe != null && cafe.getPrice() != null ? cafe.getPrice() : BigDecimal.ZERO;
        int count = entity.getCount() != null ? entity.getCount() : 0;

        ReservationCafeItemDto dto = new ReservationCafeItemDto();
        dto.setId(entity.getId());
        dto.setReservationId(entity.getReservation() != null ? entity.getReservation().getId() : null);
        dto.setCafeId(cafe != null ? cafe.getId() : null);
        dto.setCafeName(cafe != null ? cafe.getName() : null);
        dto.setUnitPrice(unitPrice);
        dto.setCount(count);
        dto.setLineTotal(unitPrice.multiply(BigDecimal.valueOf(count)));
        return dto;
    }
}
