package com.foodapp.foodorderingapp.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class OrderLineItem_OptionItem {
    private Long optionId;
    private String optionName;
    private BigDecimal price;
}
