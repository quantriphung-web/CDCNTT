package com.foodapp.foodorderingapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.foodapp.foodorderingapp.enumeration.DiscountType;

import com.foodapp.foodorderingapp.enumeration.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_discounts")
@Entity
@Getter
@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ProductDiscount {
    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
    private String name;
    private String description;
    private double discountValue;
    private ZonedDateTime createdTime;
    private Timestamp validFrom;
    private Timestamp validTo;
    private String couponCode;
    private BigDecimal maximumDiscountValue;
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    private String image;
    private int exchangeRate;
}