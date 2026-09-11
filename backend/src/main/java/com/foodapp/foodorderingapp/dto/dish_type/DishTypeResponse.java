package com.foodapp.foodorderingapp.dto.dish_type;

import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class DishTypeResponse {
    private long id;
    private String name;
}
