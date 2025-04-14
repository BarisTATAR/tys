package com.tys.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRatingRequest {
    private double ratingAverage;
    private int cleanliness;
    private int service;
    private int comfort;
    private int location;
    private int food;
}
