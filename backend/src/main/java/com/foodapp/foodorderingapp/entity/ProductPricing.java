package com.foodapp.foodorderingapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_pricings")
@Entity
@Getter
@Builder
public class ProductPricing {
    @Id
    @GeneratedValue
    private Long id;
    @OneToOne
    @JoinColumn(name = "dish_id")
    private Dish dish;
    private BigDecimal price;
    private ZonedDateTime createdTime;
    private Timestamp validFrom;
    private Timestamp validTo;
    private boolean isActive;
}