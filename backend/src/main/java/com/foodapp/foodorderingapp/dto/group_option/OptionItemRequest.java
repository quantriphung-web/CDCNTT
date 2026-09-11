package com.foodapp.foodorderingapp.dto.group_option;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class OptionItemRequest {
    private String name;
    private String description;
    @DecimalMin("0")
    private BigDecimal price;
}
