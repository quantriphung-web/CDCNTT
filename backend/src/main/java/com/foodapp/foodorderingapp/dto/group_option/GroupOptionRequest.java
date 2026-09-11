package com.foodapp.foodorderingapp.dto.group_option;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class GroupOptionRequest {
    private String name;
    private String description;
    @NotNull()
    private long restaurantId;
    @Min(0)
    private int minimum;
    @Min(0)
    private int maximum;
    private boolean isOptional;
    List<OptionItemRequest> optionItemRequests;
}
