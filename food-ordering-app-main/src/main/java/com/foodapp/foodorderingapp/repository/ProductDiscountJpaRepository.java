package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.ProductDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDiscountJpaRepository extends JpaRepository<ProductDiscount, Long> {
    Optional<ProductDiscount> findByCouponCode(String couponCode);
    List<ProductDiscount> findByRestaurantId(Long restaurantId);
}

