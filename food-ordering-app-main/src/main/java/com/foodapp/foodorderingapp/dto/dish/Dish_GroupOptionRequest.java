package com.foodapp.foodorderingapp.dto.dish;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Dish_GroupOptionRequest {
    private long dishId;
    private long groupOptionId;
}
