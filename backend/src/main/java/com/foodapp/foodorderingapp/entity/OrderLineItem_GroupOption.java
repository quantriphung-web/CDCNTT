package com.foodapp.foodorderingapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderLineItem_GroupOption {
    private Long groupOptionId;
    private String groupOptionName;
    private List<OrderLineItem_OptionItem> selectedOptions;
}

