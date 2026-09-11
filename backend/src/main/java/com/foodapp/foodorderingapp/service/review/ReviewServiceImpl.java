package com.foodapp.foodorderingapp.service.review;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.foodapp.foodorderingapp.dto.review.CreateReview;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.entity.Review;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.repository.ProductDiscountJpaRepository;
import com.foodapp.foodorderingapp.repository.RestaurantJpaRepository;
import com.foodapp.foodorderingapp.repository.ReviewJpaRepository;
import com.foodapp.foodorderingapp.repository.UserJpaRepository;

@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewJpaRepository reviewJpaRepository;
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public ReviewServiceImpl(ReviewJpaRepository reviewJpaRepository, RestaurantJpaRepository restaurantJpaRepository, UserJpaRepository userJpaRepository) {
        this.reviewJpaRepository = reviewJpaRepository;
        this.restaurantJpaRepository = restaurantJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }
    @Override
    public List<Review> getReviews(long restaurantId) {
        List<Review> reviews = reviewJpaRepository.findByRestaurantId(restaurantId);
        return reviews;
    }

    @Override
    public Review createReview(CreateReview request) {
        System.out.println(request.getRestaurantId());
        Restaurant restaurant = restaurantJpaRepository.findById(request.getRestaurantId()).orElseThrow(() -> new RuntimeException("Restaurant not found"));
        User user = userJpaRepository.findById(request.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        Review review = new Review();
                review.setRestaurant(restaurant);
                review.setUser(user);
                review.setComment(request.getComment());
                review.setRate(request.getRate());
                review.setCreatedAt(LocalDateTime.now());
        reviewJpaRepository.save(review);
        return review;
    }
    @Override
    public boolean deleteReview(long reviewId) {
        Review review = reviewJpaRepository.findById(reviewId).orElseThrow(() -> new RuntimeException("Review not found"));
        reviewJpaRepository.deleteById(reviewId);
        return true;
    }
    @Override
    public boolean updateReview(long reviewId,CreateReview request) {
        Review review = reviewJpaRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("Review not found"));
    
    review.setComment(request.getComment());
    review.setRate(request.getRate());

    reviewJpaRepository.save(review);
    return true;
    }
    
}
