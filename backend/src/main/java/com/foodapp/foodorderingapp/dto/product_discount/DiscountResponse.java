package com.foodapp.foodorderingapp.dto.product_discount;

import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import com.foodapp.foodorderingapp.dto.user_voucher.UserVoucherResponse;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.DishType;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.enumeration.DiscountType;
import com.foodapp.foodorderingapp.enumeration.DishStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Data
public class DiscountResponse {
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
    private Integer remainingUsed;
}
