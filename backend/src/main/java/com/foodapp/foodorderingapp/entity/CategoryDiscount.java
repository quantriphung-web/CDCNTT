package com.foodapp.foodorderingapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category_discounts")
@Entity
@Getter
@Builder
public class CategoryDiscount {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    @JoinColumn(name = "category_id")
    private Category category;
    private double discountValue;
    private int discountUnit;
    private ZonedDateTime createdTime;
    private Timestamp validFrom;
    private Timestamp validTo;
    private String couponCode;
    private BigDecimal minimumOrderValue;
    private BigDecimal maximumDiscountValue;
    private boolean isRedeemAllowed;
}