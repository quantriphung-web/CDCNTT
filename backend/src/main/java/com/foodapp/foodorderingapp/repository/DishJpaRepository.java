package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.dto.dish.DishNotIncludeType;
import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import com.foodapp.foodorderingapp.dto.dish.DishSearch;
import com.foodapp.foodorderingapp.entity.Category;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.DishType;
import com.foodapp.foodorderingapp.entity.Restaurant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DishJpaRepository extends JpaRepository<Dish, Long> {
    List<Dish> findDishesByCategory(Category category, Pageable pageable);

    @Query("select new com.foodapp.foodorderingapp.dto.dish.DishSearch(d.id, d.name) from Dish d where d.name LIKE %?1%")
    List<DishSearch> search(String keyword, Pageable pageable);
//    @Query("select new com.foodapp.foodorderingapp.dto.dish.DishResponse(" +
//            "d.id, " +
//            "d.name," +
//            "d.price," +
//            "d.description," +
//            "d.imageUrl," +
//            "d.status) from Dish d where d.dishType = :dishType")
//    List<DishResponse> findDishesByDishType(DishType dishType, Pageable pageable);
    @Query("select new com.foodapp.foodorderingapp.dto.dish.DishNotIncludeType(d.id, d.name, d.price, d.description, d.imageUrl, d.createdAt) from Dish d where d.dishType.id = ?1")
    List<DishNotIncludeType> findDishesByDishType(long dishTypeId, Pageable pageable);
    List<Dish> findDishesByRestaurant(Restaurant restaurant, Pageable pageable);
    @Query("select d from Dish d where d.restaurant.id = ?1 and d.category.id = ?2")
    List<Dish> findDishesByRestaurantAndCategory(long restaurantId, long categoryId, Pageable pageable);
    List<Dish> findByName(String name);

}
