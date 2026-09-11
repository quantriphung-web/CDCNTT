package com.foodapp.foodorderingapp.dto.restaurant;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodapp.foodorderingapp.dto.address.CreateAddress;
import com.foodapp.foodorderingapp.dto.category.NewCategoryRequest;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class RestaurantRequest {
    private long restaurantId;
    private String menuImageUrl;
    private String photoUrls;
    private String imageUrl;
    private String coverImageUrl;
    @NotEmpty
    private String name;
    private String mainDish;
    private long ownerId;
    private String address;
//    @JsonProperty("addressRequest")
//    private CreateAddress createAddress;
    private String description;
    private List<NewCategoryRequest> categories;
}
