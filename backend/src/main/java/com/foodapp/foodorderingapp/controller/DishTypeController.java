package com.foodapp.foodorderingapp.controller;
import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeRequest;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeWithDishResponse;
import com.foodapp.foodorderingapp.service.dish.DishService;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeOverview;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import com.foodapp.foodorderingapp.entity.DishType;
import com.foodapp.foodorderingapp.enumeration.DishClassification;
import com.foodapp.foodorderingapp.service.dish_type.DishTypeService;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/dish_types")
@RequiredArgsConstructor
public class DishTypeController {
    private final DishTypeService dishTypeService;
    private final DishService dishService;
     @GetMapping("/dishes/{id}")
    public ResponseEntity<List<DishResponse>> getDishesByDishType(
        @PathVariable long id,
        @RequestParam(defaultValue = "10") int limit, 
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String priceSort,
        @RequestParam(required = false) DishClassification dishClassification
    ) {
        return ResponseEntity.ok(dishService.getDishesByDishType(id, limit, page, priceSort, dishClassification));
    }
    @GetMapping("/overview")
    public ResponseEntity<List<DishTypeOverview>> getDishTypesOverview() {
        
        return ResponseEntity.ok(dishTypeService.getAllDishTypesWithTopThreeDishes());
    }
    @GetMapping()
    public ResponseEntity<List<DishTypeWithDishResponse>> getAll() {
        return ResponseEntity.ok(dishTypeService.getAllDishTypes());
    }
   

    @PostMapping("/seed")
    public ResponseEntity<List<DishType>> seed() {
        return ResponseEntity.ok(dishTypeService.seed());
    }

    @PostMapping()
    public ResponseEntity<DishType> create(@Valid  @RequestBody DishTypeRequest dishTypeRequest) {
        return ResponseEntity.ok(dishTypeService.create(dishTypeRequest));
    }
}
