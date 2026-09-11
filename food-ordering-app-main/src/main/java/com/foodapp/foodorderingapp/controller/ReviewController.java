package com.foodapp.foodorderingapp.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import com.foodapp.foodorderingapp.dto.restaurant.RestaurantSearch;
import com.foodapp.foodorderingapp.dto.review.CreateReview;
import com.foodapp.foodorderingapp.entity.Review;
import com.foodapp.foodorderingapp.service.restaurant.RestaurantService;
import com.foodapp.foodorderingapp.service.review.ReviewService;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    @GetMapping("/{restaurantId}")
    public ResponseEntity<List<Review >> getReviews(@PathVariable Long restaurantId) throws Exception {
        return ResponseEntity.ok(reviewService.getReviews(restaurantId));
    }
    @PostMapping()
    public ResponseEntity<Review> createReview(@RequestBody CreateReview request) throws Exception {
        System.out.println(request.getRate());
        return ResponseEntity.ok(reviewService.createReview(request));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateReview(@PathVariable long id, @RequestBody CreateReview createReview) {
        return ResponseEntity.ok(reviewService.updateReview(id, createReview));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteReview(@PathVariable long id) {
        return ResponseEntity.ok(reviewService.deleteReview(id));
    }


    
}
