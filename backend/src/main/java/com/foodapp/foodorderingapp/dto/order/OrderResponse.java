package com.foodapp.foodorderingapp.dto.order;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.foodapp.foodorderingapp.entity.Order;
import com.foodapp.foodorderingapp.entity.OrderLineItem;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.enumeration.DeliveryStatus;
import com.foodapp.foodorderingapp.enumeration.OrderStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
@Data
public class OrderResponse {
    private Long id;
    private User user;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;
    private String failureMessages;
    private BigDecimal price;
    private String address;
    private List<OrderLineItemResponse> items;
}
