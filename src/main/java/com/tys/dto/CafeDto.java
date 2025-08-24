package com.tys.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class CafeDto {
    private Long id;
    private String name;
    private BigDecimal price;
}
