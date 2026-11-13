package com.tys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCafeItemDto {
    private Long id;
    private Long reservationId;
    private Long cafeId;
    private String cafeName;
    private BigDecimal unitPrice;
    private Integer count;
    private BigDecimal lineTotal;
}
