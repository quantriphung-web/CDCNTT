package com.foodapp.foodorderingapp.dto.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodapp.foodorderingapp.entity.Voucher;
import com.foodapp.foodorderingapp.enumeration.OrderStatus;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private String address;
    List<Long> cartItemIds;
    private OrderStatus orderStatus;
    private List<Long> voucherIds;
}
