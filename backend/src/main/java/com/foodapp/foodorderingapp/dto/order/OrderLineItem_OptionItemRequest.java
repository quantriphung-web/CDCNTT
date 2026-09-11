package com.foodapp.foodorderingapp.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineItem_OptionItemRequest {
    private long optionItemId;
    private int quantity;
}
