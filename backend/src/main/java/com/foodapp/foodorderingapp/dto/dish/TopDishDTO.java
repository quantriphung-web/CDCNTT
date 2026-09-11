package com.foodapp.foodorderingapp.dto.dish;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TopDishDTO {
    private Long dishId;
    private String dishName;
    private Long totalQuantity;
    private Double salePercentage;
}
