
package com.foodapp.foodorderingapp.dto.restaurant;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestaurantResponseForDish {
    private Long id;
    private String name;
}

