package com.tys.mapper;

import com.tys.dto.ReservationCafeItemDto;
import com.tys.model.Cafe;
import com.tys.model.Reservation;
import com.tys.model.ReservationCafeItem;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public class ReservationCafeItemMapper {
    public ReservationCafeItem toEntity(ReservationCafeItemDto dto, Reservation reservation, Cafe cafe) {
        return ReservationCafeItem.builder()
                .reservation(reservation)
                .cafe(cafe)
                .count(dto.getCount())
                .build();
    }

    public ReservationCafeItemDto toDto(ReservationCafeItem entity) {
        return new ReservationCafeItemDto(
                entity.getReservation().getId(),
                entity.getCafe().getId(),
                entity.getCount()
        );
    }
}
