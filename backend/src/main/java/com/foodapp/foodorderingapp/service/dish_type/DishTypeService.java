package com.foodapp.foodorderingapp.service.dish_type;

import java.util.List;

import com.foodapp.foodorderingapp.dto.dish_type.DishTypeRequest;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeWithDishResponse;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeOverview;
import com.foodapp.foodorderingapp.entity.DishType;
import org.springframework.data.domain.Pageable;

public interface DishTypeService {
    List<DishTypeWithDishResponse> getAllDishTypes();
    List<DishTypeOverview> getAllDishTypesWithTopThreeDishes();
    List<DishType> seed();
    DishType create(DishTypeRequest dishTypeRequest);
    DishType update(long id, DishTypeRequest dishTypeRequest) throws Exception;
    DishType delete(long id) throws Exception;
}
