package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRatingRequest {
    private Double ratingAverage;
    private Integer cleanliness;
    private Integer service;
    private Integer comfort;
    private Integer location;
    private Integer food;
}
