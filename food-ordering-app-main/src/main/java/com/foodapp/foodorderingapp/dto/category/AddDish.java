package com.foodapp.foodorderingapp.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddDish {
    private Long categoryId;
    private Long dishId;
}
