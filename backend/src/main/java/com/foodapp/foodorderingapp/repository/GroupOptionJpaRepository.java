package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.GroupOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupOptionJpaRepository extends JpaRepository<GroupOption, Long> {
    List<GroupOption> findGroupOptionByRestaurantId(long restaurantId);
}

