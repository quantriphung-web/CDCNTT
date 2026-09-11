package com.foodapp.foodorderingapp.entity;

import jakarta.persistence.*;
import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "dish_group_options")
public class Dish_GroupOption {
    @EmbeddedId
    private Dish_GroupOptionId dish_groupOptionId;
}

