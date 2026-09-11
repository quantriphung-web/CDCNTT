package com.foodapp.foodorderingapp.service.review;
import java.util.List;

import com.foodapp.foodorderingapp.dto.review.CreateReview;
import com.foodapp.foodorderingapp.entity.Review;

public interface ReviewService {
    List<Review> getReviews(long restaurantId);
    Review createReview(CreateReview request);
    boolean deleteReview(long reviewId);
    boolean updateReview(long reviewId, CreateReview request);
}
