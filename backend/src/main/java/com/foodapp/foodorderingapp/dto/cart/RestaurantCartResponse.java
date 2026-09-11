package com.foodapp.foodorderingapp.dto.cart;

import com.foodapp.foodorderingapp.entity.Restaurant;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class RestaurantCartResponse {
    private Restaurant restaurant;
    private BigDecimal totalPrice;
    private int quantity;
}
