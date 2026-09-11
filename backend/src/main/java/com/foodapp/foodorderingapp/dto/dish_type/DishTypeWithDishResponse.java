package com.foodapp.foodorderingapp.dto.dish_type;

import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DishTypeWithDishResponse {
    private long id;
    private String name;
    private List<DishResponse> dishes;
}
