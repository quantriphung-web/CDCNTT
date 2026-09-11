package com.foodapp.foodorderingapp.helper;

import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.repository.DishJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
public class DishHelper {
    private final DishJpaRepository dishJpaRepository;

    public DishHelper(DishJpaRepository dishJpaRepository) {
        this.dishJpaRepository = dishJpaRepository;
    }
    public Optional<Dish> findDishById(Long id){
        return dishJpaRepository.findById(id);
    }
}
