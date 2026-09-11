package com.foodapp.foodorderingapp.service.restaurant;

import com.foodapp.foodorderingapp.dto.restaurant.AddCategory;
import com.foodapp.foodorderingapp.dto.restaurant.RestaurantRequest;
import com.foodapp.foodorderingapp.dto.restaurant.RestaurantSearch;
import com.foodapp.foodorderingapp.entity.Address;
import com.foodapp.foodorderingapp.entity.Category;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.enumeration.RestaurantStatus;
import com.foodapp.foodorderingapp.helper.UserHelper;
import com.foodapp.foodorderingapp.repository.CategoryJpaRepository;
import com.foodapp.foodorderingapp.repository.RestaurantJpaRepository;
import com.foodapp.foodorderingapp.repository.UserJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService{
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    @Transactional
    public Restaurant createRestaurant(RestaurantRequest restaurantRequest) {
        Optional<User> user = userJpaRepository.findById(restaurantRequest.getOwnerId());
        if(user.isEmpty()) throw new IllegalArgumentException("Not found user");
        Restaurant restaurant = Restaurant.builder().name(restaurantRequest.getName())
                .imageUrl(restaurantRequest.getImageUrl())
                .name(restaurantRequest.getName())
                .coverImageUrl(restaurantRequest.getCoverImageUrl())
                .mainDish(restaurantRequest.getMainDish())
                .owner(user.get())
                .status(RestaurantStatus.CREATED)
                .address(restaurantRequest.getAddress())
                .description(restaurantRequest.getDescription())
                .menuImageUrl(restaurantRequest.getMenuImageUrl())
                .photoUrls(restaurantRequest.getPhotoUrls())
                .categories(new ArrayList<>()).build();

        Restaurant restaurantData = restaurantJpaRepository.save(restaurant);
        List<Category> categories = restaurantRequest.getCategories().stream().map(item -> {
           Category category = Category.builder()
                   .restaurant(restaurantData)
                   .name(item.getName())
                   .dishQuantity(0)
                   .isActive(true)
                   .dishes(new ArrayList<>())
                   .build();
            return categoryJpaRepository.save(category);
        }).toList();
        restaurantData.setCategories(categories);
        return restaurantData;
    }
    @Override
    public Restaurant getRestaurantById(long id) throws IllegalArgumentException {
        return restaurantJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
    }

    @Override
    public List<Restaurant> getAllRestaurants(Integer pageNo, Integer pageSize) throws IllegalArgumentException {
        Pageable paging = PageRequest.of(pageNo, pageSize);
        Page<Restaurant> pagedResult = restaurantJpaRepository.findAll(paging);
        if(pagedResult.hasContent()) {
            return pagedResult.getContent();
        } else {
            return new ArrayList<Restaurant>();
        }
    }

    @Override
    public Restaurant addCategory(AddCategory request) {
        Category category = categoryJpaRepository.findById(request.getCategoryId()).orElseThrow(()-> new EntityNotFoundException("not found category"));
        Restaurant restaurant = restaurantJpaRepository.findById(request.getRestaurantId()).orElseThrow(()-> new EntityNotFoundException("not found restaurant"));
        restaurant.getCategories().add(category);
        return restaurantJpaRepository.save(restaurant);
    }

    @Override
    @Transactional
    public Restaurant updateRestaurant(long restaurantId, RestaurantRequest createRestaurantRequest) {
        Restaurant existingRestaurant = getRestaurantById(restaurantId);
        existingRestaurant.setName(createRestaurantRequest.getName());
        return restaurantJpaRepository.save(existingRestaurant);
    }


    @Override
    @Transactional
    public Restaurant changeStatus(long id, RestaurantStatus status) throws Exception {
         Restaurant restaurant = restaurantJpaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));
         restaurant.setStatus(status);
         return restaurant;
    }

    @Override
    public List<RestaurantSearch> search(String keyword) {
        Pageable pageable = PageRequest.of(0, 5);
        return restaurantJpaRepository.search(keyword, pageable);
    }
    @Override
    public List<Restaurant> getByOwnerId(long ownerId) {
        return restaurantJpaRepository.findByOwnerId(ownerId);
    }
    @Override
    public List<Restaurant> getRestaurantByDistance(double latitude, double longitude, double distance, String keyword) {
        final double R = 6371;
        List<Restaurant> res = new ArrayList<>();
        
        restaurantJpaRepository.findByNameContainingIgnoreCase(keyword).forEach(restaurant -> {
            if(restaurant.getLongitude() != null && restaurant.getLatitude() != null) {
                double lat2 = Double.parseDouble(restaurant.getLatitude()) ;
                double lon2 = Double.parseDouble(restaurant.getLongitude());
            
                double lat1Rad = Math.toRadians(latitude);
                double lon1Rad = Math.toRadians(longitude);
                double lat2Rad = Math.toRadians(lat2);
                double lon2Rad = Math.toRadians(lon2);
                double dlon = lon2Rad - lon1Rad;
                double dlat = lat2Rad - lat1Rad;
                double a = Math.pow(Math.sin(dlat / 2), 2)
                        + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                        * Math.pow(Math.sin(dlon / 2),2);
                double c = 2 * Math.asin(Math.sqrt(a));
                double distanceInKm = R * c;
                if(distanceInKm <= distance) {
                    res.add(restaurant);
                }
            }   
           
        });
        return res;
    }

}
