package com.foodapp.foodorderingapp.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class Dish_GroupOptionId implements Serializable {
    @ManyToOne
    private GroupOption groupOption;
    @ManyToOne
    private Dish dish;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dish_GroupOptionId dish_groupOptionId = (Dish_GroupOptionId) o;
        return groupOption.getId().equals(dish_groupOptionId.groupOption.getId()) &&
                dish.getId().equals(dish_groupOptionId.dish.getId());
    }
    @Override
    public int hashCode() {
        return Objects.hash(groupOption, dish);
    }
}
