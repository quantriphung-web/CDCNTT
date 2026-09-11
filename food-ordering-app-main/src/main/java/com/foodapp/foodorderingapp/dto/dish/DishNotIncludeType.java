package com.foodapp.foodorderingapp.dto.dish;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.foodapp.foodorderingapp.entity.DishType;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.enumeration.DishStatus;

import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
@Getter
@Setter
@AllArgsConstructor
public class DishNotIncludeType {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;
}
