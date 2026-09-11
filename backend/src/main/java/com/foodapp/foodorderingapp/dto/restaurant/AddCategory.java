package com.foodapp.foodorderingapp.dto.restaurant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddCategory {
    private Long categoryId;
    private Long restaurantId;
}
