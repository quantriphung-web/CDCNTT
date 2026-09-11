package com.foodapp.foodorderingapp.service.dish;

import com.foodapp.foodorderingapp.dto.dish.DishNotIncludeType;
import com.foodapp.foodorderingapp.dto.dish.DishRequest;
import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import com.foodapp.foodorderingapp.dto.dish.DishSearch;
import com.foodapp.foodorderingapp.dto.group_option.GroupOptionResponse;
import com.foodapp.foodorderingapp.entity.*;
import com.foodapp.foodorderingapp.enumeration.DishClassification;
import com.foodapp.foodorderingapp.enumeration.DishStatus;
import com.foodapp.foodorderingapp.exception.DataNotFoundException;
import com.foodapp.foodorderingapp.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.jsoup.Jsoup;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.IOException;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONObject;
import org.json.JSONArray;
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private final CategoryJpaRepository categoryJpaRepository;
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final DishJpaRepository dishJpaRepository;
    private final DishTypeJpaRepository dishTypeJpaRepository;
    private final GroupOptionJpaRepository groupOptionJpaRepository;
    private final Dish_GroupOptionJpaRepository dish_groupOptionJpaRepository;
    private final ModelMapper modelMapper;
    @Value("${foodapp.app.unsplashApiKey}")
    private String unsplashApiKey;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public DishResponse getDishById(long dishId) {
        Optional<Dish> dish = dishJpaRepository.findById(dishId);
        if (dish.isPresent()) {
            List<String> imageUrls = fetchImageUrls(dish.get().getName());
            if (imageUrls.size() >= 3) {
                dish.get().setImageUrl(String.join(", ", imageUrls));
            } else {
                dish.get().setImageUrl("https://ik.imagekit.io/munchery/blog/tr:w-768/the-10-dishes-that-define-moroccan-cuisine.jpeg, https://giavivietan.com/wp-content/uploads/2020/01/VIANCO-Hinh-CHUP-T%C3%94-CA-RI-1-scaled.jpg, https://cms-prod.s3-sgn09.fptcloud.com/cach_nau_ca_ri_ga_tai_nha_bao_ngon_va_chuan_vi_an_hoai_khong_chan_1_c47c7657bc.jpg");
            }
            List<Dish_GroupOption> dishGroupOptions = dish_groupOptionJpaRepository.findByDishId(dishId);
            List<GroupOptionResponse> groupOptions = dishGroupOptions.stream()
                    .map(dishGroupOption -> {
                        return modelMapper.map( dishGroupOption.getDish_groupOptionId().getGroupOption(), GroupOptionResponse.class);
                    })
                    .toList();
            DishResponse dishResponse = modelMapper.map(dish, DishResponse.class);
            dishResponse.setOptions(groupOptions);
            return dishResponse;
        } else
            throw new DataNotFoundException("Can't not find dish with id" + dishId);
    }
    @Override
    public List<String> fetchImageUrls(String query) {
    Dish dish = dishJpaRepository.findByName(query).get(0);
        String ACCESS_TOKEN = unsplashApiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", ACCESS_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        if(!dish.getImageUrl().startsWith("https://images.pexels.com")) {
            String searchUrl = "https://api.pexels.com/v1/search?query=" + dish.getName() + "&per_page=3";
            List<String> imageUrls = new ArrayList<>();  
            try {
                ResponseEntity<String> response = restTemplate.exchange(searchUrl, HttpMethod.GET, entity, String.class);
                JSONObject jsonResponse = new JSONObject(response.getBody());
                JSONArray results = jsonResponse.getJSONArray("photos");
                for (int i = 0; i < results.length(); i++) {
                    String url = results.getJSONObject(i).getJSONObject("src").getString("original");
                    imageUrls.add(url);
                }
            
            } catch (Exception e) {
                imageUrls = new ArrayList<>();
                imageUrls.add("https://ik.imagekit.io/munchery/blog/tr:w-768/the-10-dishes-that-define-moroccan-cuisine.jpeg");
                imageUrls.add("https://giavivietan.com/wp-content/uploads/2020/01/VIANCO-Hinh-CHUP-T%C3%94-CA-RI-1-scaled.jpg");
                imageUrls.add("https://cms-prod.s3-sgn09.fptcloud.com/cach_nau_ca_ri_ga_tai_nha_bao_ngon_va_chuan_vi_an_hoai_khong_chan_1_c47c7657bc.jpg");
            }
            dish.setImageUrl(String.join(", ", imageUrls));
            dishJpaRepository.save(dish);
            return imageUrls;
        }
        return dish.getImageUrl().contains(",") ? List.of(dish.getImageUrl().split(", ")) : List.of(dish.getImageUrl());
       
    }

    @Override
    @Transactional
    public Dish addDish(DishRequest dishRequest) {
        Restaurant existingRestaurant = restaurantJpaRepository
                .findById(dishRequest.getRestaurantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot find restaurant with id: " + dishRequest.getRestaurantId()));

        Category existingCategory = categoryJpaRepository
                .findById(dishRequest.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot find category with id: " + dishRequest.getCategoryId()));
        DishType dishType = dishTypeJpaRepository.findById(dishRequest.getDishTypeId()).orElseThrow(
                () -> new IllegalArgumentException("Can't not find dish type with id" + dishRequest.getDishTypeId()));
        Dish newDish = Dish.builder()
                .name(dishRequest.getName())
                .price(dishRequest.getPrice())
                .imageUrl(dishRequest.getImageUrl())
                .description(dishRequest.getDescription())
                .restaurant(existingRestaurant)
                .category(existingCategory)
                .dishType(dishType)
                .status(DishStatus.ACTIVE)
                .build();
        return dishJpaRepository.save(newDish);
    }

    @Override
    @Transactional
    public Dish updateDish(long id, DishRequest dishRequest) throws Exception {
        Dish dish = dishJpaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(
                "Can't find dish with id: " + id
        ));
        categoryJpaRepository
                .findById(dishRequest.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Cannot find category with id: " + dishRequest.getCategoryId()));
        if (dishRequest.getName() != null && !dishRequest.getName().isEmpty()) {
            dish.setName(dishRequest.getName());
        }
        if (dishRequest.getDescription() != null && !dishRequest.getDescription().isEmpty()) {
            dish.setDescription(dishRequest.getDescription());
        }
        if (dishRequest.getImageUrl() != null && !dishRequest.getImageUrl().isEmpty()) {
            dish.setImageUrl(dishRequest.getImageUrl());
        }
        dish.setPrice(dishRequest.getPrice());
        return dish;
    }

    @Override
    @Transactional
    public Dish deleteDish(long id) throws Exception {
        Dish dish = dishJpaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(
                "Can't find dish with id: " + id
        ));
        dish.setStatus(DishStatus.DELETED);
        return dish;
    }

    @Override
    public Dish_GroupOption addGroupOptionToDish(long id, long groupOptionId) throws Exception {
        Dish dish = dishJpaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(
                "Can't find dish with id: " + id
        ));
        GroupOption groupOption = groupOptionJpaRepository.findById(groupOptionId)
                .orElseThrow(() -> new DataNotFoundException("Not found group optin with id" + groupOptionId));
        Dish_GroupOptionId dish_groupOptionId = Dish_GroupOptionId.builder()
                .dish(dish)
                .groupOption(groupOption)
                .build();
        Dish_GroupOption dish_groupOption = Dish_GroupOption.builder()
                .dish_groupOptionId(dish_groupOptionId)
                .build();
        return dish_groupOptionJpaRepository.save(dish_groupOption);
    }

    @Override
    public List<DishResponse> getDishesByCategory(long restaurantId, long categoryId, int page, int limit) {
        Category existingCategory = categoryJpaRepository
                .findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot find category with id: " + String.valueOf(categoryId)));
        Restaurant restaurant = restaurantJpaRepository
        .findById(restaurantId)
        .orElseThrow(() -> new IllegalArgumentException(
                "Cannot find restaurant with id: " + String.valueOf(restaurantId)));
        List<Dish> dishesResponse = dishJpaRepository.findDishesByRestaurantAndCategory(restaurantId, categoryId, PageRequest.of(page, limit));
        for (Dish dishResponse : dishesResponse) {
            List<String> imageUrls = fetchImageUrls(dishResponse.getName());
            if (imageUrls.size() >= 3) {
                dishResponse.setImageUrl(String.join(", ", imageUrls));
            } else {
                dishResponse.setImageUrl("https://ik.imagekit.io/munchery/blog/tr:w-768/the-10-dishes-that-define-moroccan-cuisine.jpeg, https://giavivietan.com/wp-content/uploads/2020/01/VIANCO-Hinh-CHUP-T%C3%94-CA-RI-1-scaled.jpg, https://cms-prod.s3-sgn09.fptcloud.com/cach_nau_ca_ri_ga_tai_nha_bao_ngon_va_chuan_vi_an_hoai_khong_chan_1_c47c7657bc.jpg");
            }
        }
        return dishesResponse.stream()
                .map(item -> modelMapper.map(item, DishResponse.class))
                .collect(Collectors.toList());
        
    }

    @Override
    public List<DishSearch> search(String keyword) {
        Pageable pageable = PageRequest.of(0, 5);
        return dishJpaRepository.search(keyword, pageable);
    }

    @Override
    public List<Dish> findAll() {
        List<Dish> dishes = dishJpaRepository.findAll();
        Hibernate.initialize(dishes);
        return dishes;
    }
    @Override
    public List<DishResponse> findDishesByRestaurant(long restaurantId, int limit, int page) {
        Restaurant restaurant = restaurantJpaRepository
                .findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot find restaurant with id: " + String.valueOf(restaurantId)));
        System.out.println(restaurant);
        
        Pageable request = PageRequest.of(page, limit);
        List<Dish> dishes = dishJpaRepository.findDishesByRestaurant(restaurant, request);

        for (Dish dishResponse : dishes) {
            List<String> imageUrls = fetchImageUrls(dishResponse.getName());
            if (imageUrls.size() >= 3) {
                dishResponse.setImageUrl(String.join(", ", imageUrls));
            } else {
                dishResponse.setImageUrl("https://ik.imagekit.io/munchery/blog/tr:w-768/the-10-dishes-that-define-moroccan-cuisine.jpeg, https://giavivietan.com/wp-content/uploads/2020/01/VIANCO-Hinh-CHUP-T%C3%94-CA-RI-1-scaled.jpg, https://cms-prod.s3-sgn09.fptcloud.com/cach_nau_ca_ri_ga_tai_nha_bao_ngon_va_chuan_vi_an_hoai_khong_chan_1_c47c7657bc.jpg");
            }
        }
        System.out.println(dishes.size());
        return dishes.stream()
        .map(item -> modelMapper.map(item, DishResponse.class))
        .collect(Collectors.toList());
    }

    @Override
    public List<DishResponse> getDishesByDishType(long dishTypeId, int limit, int page, String priceSort, DishClassification dishClassification) {
        DishType dishType = dishTypeJpaRepository
                .findById(dishTypeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cannot find restaurant with id: " + String.valueOf(dishTypeId)));
        Pageable request = PageRequest.of(page, limit);
        List<DishNotIncludeType> dishes = dishJpaRepository.findDishesByDishType(dishType.getId(), request);
        List<DishResponse> dishResponses = dishes.stream()
                    .map(dish -> {
                        DishResponse newDish = new DishResponse();
                        List<String> imageUrls = fetchImageUrls(dish.getName());
            if (imageUrls.size() >= 3) {
                newDish.setImageUrl(String.join(", ", imageUrls));
            } else {
                newDish.setImageUrl("https://ik.imagekit.io/munchery/blog/tr:w-768/the-10-dishes-that-define-moroccan-cuisine.jpeg, https://giavivietan.com/wp-content/uploads/2020/01/VIANCO-Hinh-CHUP-T%C3%94-CA-RI-1-scaled.jpg, https://cms-prod.s3-sgn09.fptcloud.com/cach_nau_ca_ri_ga_tai_nha_bao_ngon_va_chuan_vi_an_hoai_khong_chan_1_c47c7657bc.jpg");
            }
            newDish.setId(dish.getId());
            newDish.setName(dish.getName());
            newDish.setDescription(dish.getDescription());
            newDish.setPrice(dish.getPrice());
            newDish.setCreatedAt(dish.getCreatedAt());
            return newDish;
                    })
                    .collect(Collectors.toList());
        if(dishClassification != null) {
            switch (dishClassification) {
                case RELATED:
                    break;
                case LATEST:
                dishResponses.sort((dish1, dish2) -> dish2.getCreatedAt().compareTo(dish1.getCreatedAt()));
                    break;
                case BEST_SELLER:
                    break;
                default:
                    break;
            }
            return dishResponses;
        }
        if(priceSort != null) {
            if(priceSort.equals("asc")) {
                dishResponses.sort((dish1, dish2) -> dish1.getPrice().compareTo(dish2.getPrice()));
            }
            else dishResponses.sort((dish1, dish2) -> dish2.getPrice().compareTo(dish1.getPrice()));
        }
        return dishResponses;
    }

    @Override
    public List<DishResponse> getRecommendedDishes(List<String> names) {
        List<Dish> dishes = new ArrayList<>();
        names.forEach(id -> {
            List<Dish> res = dishJpaRepository.findByName(id);
            if(!res.isEmpty())
            dishes.add(res.get(0));
        });
        for (Dish dishResponse : dishes) {
            List<String> imageUrls = fetchImageUrls(dishResponse.getName());
            if (imageUrls.size() >= 3) {
                dishResponse.setImageUrl(String.join(", ", imageUrls));
            } else {
                dishResponse.setImageUrl("https://ik.imagekit.io/munchery/blog/tr:w-768/the-10-dishes-that-define-moroccan-cuisine.jpeg, https://giavivietan.com/wp-content/uploads/2020/01/VIANCO-Hinh-CHUP-T%C3%94-CA-RI-1-scaled.jpg, https://cms-prod.s3-sgn09.fptcloud.com/cach_nau_ca_ri_ga_tai_nha_bao_ngon_va_chuan_vi_an_hoai_khong_chan_1_c47c7657bc.jpg");
            }
        }
        return dishes.stream()
                .map(item -> modelMapper.map(item, DishResponse.class))
                .collect(Collectors.toList());
    }

}
