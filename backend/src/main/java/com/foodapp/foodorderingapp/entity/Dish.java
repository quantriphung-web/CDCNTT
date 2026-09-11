package com.foodapp.foodorderingapp.entity;
import com.fasterxml.jackson.annotation.*;
import com.foodapp.foodorderingapp.enumeration.DishStatus;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.UpdateTimestamp;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dishes")
@Entity
@Builder
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "RESTAURANT_ID")
    @JsonManagedReference
    @JsonIgnoreProperties("categories")
    @JsonIgnore
    private Restaurant restaurant;

    @ManyToOne()
    @JoinColumn(name = "CATEGORY_ID")
    @JsonBackReference
    private Category category;
    private String name;
    private BigDecimal price;
    private String description;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    private DishStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_type_id")
    @JsonIgnore
    private DishType dishType;
     @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish that = (Dish) o;
        return id.equals(that.id) && category.equals(that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category);
    }
}

