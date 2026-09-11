package com.foodapp.foodorderingapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Table(name = "dish_types")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Setter
@Getter
public class DishType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    @OneToMany(mappedBy = "dishType")
    @JsonBackReference
    private List<Dish> dishes;
}
