package com.foodapp.foodorderingapp.dto.product_discount;

import com.foodapp.foodorderingapp.enumeration.DiscountType;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductDiscountRequest {
    private Long id;
    private String name;
    private double discountValue;
    private ZonedDateTime createdTime;
    private Timestamp validFrom;
    private Timestamp validTo;
    private String couponCode;
    private BigDecimal maximumDiscountValue;
    private DiscountType discountType;
    private String description;
    private String image;
    private int exchangeRate;
}
