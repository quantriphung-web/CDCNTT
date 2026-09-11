package com.foodapp.foodorderingapp.dto.order;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderLineItem_GroupOptionRequest {
    private long groupOptionId;
    List<OrderLineItem_OptionItemRequest> orderLineItem_optionItems;
}
