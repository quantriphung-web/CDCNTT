package com.foodapp.foodorderingapp.dto.dish;

import java.math.BigDecimal;

import com.foodapp.foodorderingapp.entity.Category;
import com.foodapp.foodorderingapp.entity.DishType;
import com.foodapp.foodorderingapp.enumeration.DishStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
public class FeaturedDish {
    private long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;

}
