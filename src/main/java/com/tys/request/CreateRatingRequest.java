package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRatingRequest {
    private BigDecimal ratingAverage;
    private Integer cleanliness;
    private Integer service;
    private Integer comfort;
    private Integer location;
    private Integer food;
}
