package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.dto.restaurant.RestaurantSearch;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.Restaurant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RestaurantJpaRepository extends JpaRepository<Restaurant, Long> {
    @Query("select new com.foodapp.foodorderingapp.dto.restaurant.RestaurantSearch(r.id, r.name) from Restaurant r where r.name LIKE %?1%")
    List<RestaurantSearch> search(String keyword, Pageable pageble);
    List<Restaurant> findByOwnerId(long ownerId);
    List<Restaurant> findByNameContainingIgnoreCase(String keyword);

}
