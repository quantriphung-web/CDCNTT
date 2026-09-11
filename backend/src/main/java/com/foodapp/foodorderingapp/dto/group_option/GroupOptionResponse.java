package com.foodapp.foodorderingapp.dto.group_option;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.foodapp.foodorderingapp.entity.OptionItem;
import com.foodapp.foodorderingapp.entity.Restaurant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class GroupOptionResponse {
    private Long id;
    private List<OptionItem> optionItems;
    private String name;
    private int minimum;
    private int maximum;
    private boolean isOptional;

}
