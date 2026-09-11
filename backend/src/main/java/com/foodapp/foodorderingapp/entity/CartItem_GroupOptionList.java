package com.foodapp.foodorderingapp.entity;




import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem_GroupOptionList {
    private List<CartItem_GroupOption> selectedOptions;
}
