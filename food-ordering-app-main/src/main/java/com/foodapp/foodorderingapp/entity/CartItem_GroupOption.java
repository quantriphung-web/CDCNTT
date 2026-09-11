package com.foodapp.foodorderingapp.entity;

import lombok.*;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem_GroupOption {
        private Long groupOptionId;
        private List<Long> selectedOptions;
}