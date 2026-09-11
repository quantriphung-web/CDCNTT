package com.foodapp.foodorderingapp.dto.cart;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartOfDishRequest {
    private Long dishId;
    private Long userId;
}

