package com.foodapp.foodorderingapp.dto.dish;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;



@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class DishSearch {
    private Long id;
    private String name;
}