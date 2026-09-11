package com.foodapp.foodorderingapp.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class OrderLineItemRequest {
    private Long dishId;
    List<OrderLineItem_GroupOptionRequest> orderLineItemGroupOptionRequests;
    private int quantity;
}
