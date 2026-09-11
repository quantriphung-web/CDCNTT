package com.foodapp.foodorderingapp.dto.cart;

import com.foodapp.foodorderingapp.converter.CartOptionConverter;
import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import com.foodapp.foodorderingapp.entity.CartItem_GroupOptionList;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class CartItemResponse {
    private Long id;
    private DishResponse dish;
    private Integer quantity;
    private BigDecimal total;
    private CartItem_GroupOptionList options;
    private Date createdAt;
}

