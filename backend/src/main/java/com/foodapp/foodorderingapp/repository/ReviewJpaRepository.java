package com.foodapp.foodorderingapp.repository;
import com.foodapp.foodorderingapp.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReviewJpaRepository extends JpaRepository<Review, Long> {
    List<Review> findByRestaurantId(Long restaurantId);
}

