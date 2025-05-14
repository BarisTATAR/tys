package com.tys.service;

import com.tys.mapper.RatingMapper;
import com.tys.model.Rating;
import com.tys.repository.RatingRepository;
import com.tys.request.CreateRatingRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;

    public void createRating(CreateRatingRequest request) {
        Rating rating = ratingMapper.createRatingRequestToEntity(request);
        ratingRepository.save(rating);
    }

    public Rating getRatingById(Long id) {
        return ratingRepository.findById(id).orElseThrow(() -> new RuntimeException("Rating not found with Id: " + id));
    }
}
