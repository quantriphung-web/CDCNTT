package com.foodapp.foodorderingapp.dto.dish_type;
import java.util.ArrayList;
import java.util.List;
import com.foodapp.foodorderingapp.dto.dish.FeaturedDish;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DishTypeOverview {
    private long id;
    private String name;
    private List<FeaturedDish> dishes;
    public DishTypeOverview() {
    }
    public DishTypeOverview(Long id, String name) {
        this.id = id;
        this.name = name;
        this.dishes = new ArrayList<>();
    }

}
