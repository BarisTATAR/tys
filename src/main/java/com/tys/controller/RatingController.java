package com.tys.controller;

import com.tys.model.Rating;
import com.tys.request.CreateRatingRequest;
import com.tys.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rating")
public class RatingController {

    private final RatingService ratingService;
    @PostMapping("/create")
    public ResponseEntity<Void> createRating(@RequestBody CreateRatingRequest request) {
        ratingService.createRating(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Rating> getRatingById(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.getRatingById(id));
    }
}
