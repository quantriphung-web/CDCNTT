package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.enumeration.OrderStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.foodapp.foodorderingapp.entity.Order;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;;
@Repository
public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> findByUser(Long userId);
    List<Order> findOrdersByRestaurantId(Long restaurantId);
    @Query("SELECT s FROM Order s WHERE s.restaurant = :restaurant AND s.createdAt BETWEEN :startTime AND :endTime ")
    List<Order> findByRestaurantAndCreatedAtBetween(
            @Param("restaurant") Restaurant restaurant,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

}
