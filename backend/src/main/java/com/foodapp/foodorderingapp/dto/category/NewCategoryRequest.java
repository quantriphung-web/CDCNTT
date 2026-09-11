package com.foodapp.foodorderingapp.dto.category;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewCategoryRequest {
    @NotEmpty(message = "Category's name cannot be empty")
    private String name;
    private Integer dishQuantity;
}
