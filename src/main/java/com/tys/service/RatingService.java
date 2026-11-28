package com.tys.service;

import com.tys.mapper.RatingMapper;
import com.tys.model.Rating;
import com.tys.repository.RatingRepository;
import com.tys.request.CreateRatingRequest;
import com.tys.validation.InputRules;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final RatingMapper ratingMapper;

    public void createRating(CreateRatingRequest request, Long companyId) {
        if (request == null) {
            throw new IllegalArgumentException("Değerlendirme bilgileri gereklidir.");
        }
        InputRules.rating(request.getRatingAverage(), request.getCleanliness(), request.getService(),
                request.getComfort(), request.getLocation(), request.getFood());
        Rating rating = ratingMapper.createRatingRequestToEntity(request);
        rating.setCompanyId(companyId);
        ratingRepository.save(rating);
    }

    /** Başka işletmenin değerlendirmesi ile olmayan kayıt ayırt edilemez (404). */
    public Rating getRatingById(Long id, Long companyId) {
        return ratingRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Değerlendirme bulunamadı."));
    }
}
