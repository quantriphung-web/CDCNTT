package com.foodapp.foodorderingapp.dto.dish_type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DishTypeRequest {
    private String name;
}
