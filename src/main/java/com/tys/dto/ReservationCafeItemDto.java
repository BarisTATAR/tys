package com.tys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCafeItemDto {
    private Long reservationId;
    private Long cafeId;
    private Integer count;
}
