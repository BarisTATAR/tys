package com.tys.controller;

import com.tys.model.Rating;
import com.tys.request.CreateRatingRequest;
import com.tys.security.CurrentActor;
import com.tys.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rating")
public class RatingController {

    private final RatingService ratingService;
    private final CurrentActor currentActor;

    @PostMapping("/create")
    public ResponseEntity<Void> createRating(@RequestBody CreateRatingRequest request) {
        ratingService.createRating(request, currentActor.requireCompanyId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rating> getRatingById(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.getRatingById(id, currentActor.requireCompanyId()));
    }
}
