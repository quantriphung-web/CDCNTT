package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.Dish_GroupOption;
import com.foodapp.foodorderingapp.entity.Dish_GroupOptionId;
import com.foodapp.foodorderingapp.entity.GroupOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface Dish_GroupOptionJpaRepository extends JpaRepository<Dish_GroupOption, Dish_GroupOptionId> {
    @Query("SELECT dgo FROM Dish_GroupOption dgo WHERE dgo.dish_groupOptionId.dish.id = :dishId")
    List<Dish_GroupOption> findByDishId(Long dishId);
}
