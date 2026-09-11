package com.foodapp.foodorderingapp.dto.dish;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.JoinColumn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class DishRequest {
    private String name;
    @DecimalMin(value = "0",message = "Price must be greater than 0")
    private BigDecimal price;
    private String description;
    private String imageUrl;
    @NotNull()
    private long restaurantId;
    @NotNull()
    private long categoryId;
    @NotNull
    private long dishTypeId;
}
