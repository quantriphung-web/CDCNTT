package com.foodapp.foodorderingapp.dto.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.foodapp.foodorderingapp.converter.OrderLineItemOptionConverter;
import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.Order;
import com.foodapp.foodorderingapp.entity.OrderLineItem_GroupOptionList;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderLineItemResponse {
    private Long id;
    private OrderLineItem_GroupOptionList options;
   private DishResponse dish;
    private Integer quantity;
    private BigDecimal subTotal;
}
