package com.foodapp.foodorderingapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import java.util.List;


import jakarta.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "group-options")
@Entity
public class GroupOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESTAURANT_ID")
    @JsonBackReference
    private Restaurant restaurant;

    @OneToMany(mappedBy = "groupOption", cascade = CascadeType.ALL)
    private List<OptionItem> optionItems;

    private String name;
    private int minimum;
    private int maximum;
    private boolean isOptional;
}
