package com.foodapp.foodorderingapp.entity;

import com.foodapp.foodorderingapp.converter.CartOptionConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

@Data  // Includes @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
@Entity
@Builder  // Enables the builder pattern for this entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart_items")

public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @ManyToOne
    @JoinColumn(name = "DISH_ID")
    private Dish dish;

    private Integer quantity;
    private BigDecimal total;

    @Convert(converter = CartOptionConverter.class)
    @Column(name = "options", length = 500)
    private CartItem_GroupOptionList options;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)  // Indicates that createdAt is a timestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;
}
