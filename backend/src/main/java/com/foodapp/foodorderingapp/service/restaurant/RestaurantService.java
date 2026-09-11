package com.foodapp.foodorderingapp.service.restaurant;

import com.foodapp.foodorderingapp.dto.restaurant.AddCategory;
import com.foodapp.foodorderingapp.dto.restaurant.RestaurantRequest;
import com.foodapp.foodorderingapp.dto.restaurant.RestaurantSearch;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.enumeration.RestaurantStatus;

import java.util.List;

public interface RestaurantService {
    Restaurant createRestaurant(RestaurantRequest restaurant);
    Restaurant getRestaurantById(long id) throws IllegalArgumentException;
    List<Restaurant> getAllRestaurants(Integer pageNo, Integer pageSize) throws IllegalArgumentException;
    Restaurant addCategory(AddCategory request);
    Restaurant updateRestaurant(long restaurantId, RestaurantRequest createRestaurantRequest);
    Restaurant changeStatus(long id, RestaurantStatus status) throws Exception;
    List<RestaurantSearch> search(String keyword);
    List<Restaurant> getByOwnerId(long ownerId);
    List<Restaurant> getRestaurantByDistance(double latitude, double longitude, double distance, String keyword);

}
