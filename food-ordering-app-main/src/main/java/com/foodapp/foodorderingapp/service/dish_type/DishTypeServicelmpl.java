package com.foodapp.foodorderingapp.service.dish_type;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.foodapp.foodorderingapp.dto.dish.DishNotIncludeType;
import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeRequest;
import com.foodapp.foodorderingapp.dto.dish.FeaturedDish;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeWithDishResponse;
import com.foodapp.foodorderingapp.dto.dish_type.DishTypeOverview;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.repository.DishJpaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.foodapp.foodorderingapp.entity.DishType;
import com.foodapp.foodorderingapp.repository.DishTypeJpaRepository;
import com.foodapp.foodorderingapp.service.dish.DishService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.javafaker.Faker;

@Slf4j
@Service
@RequiredArgsConstructor
public class DishTypeServicelmpl implements DishTypeService {
    private final DishTypeJpaRepository dishTypeJpaRepository;
    private final DishJpaRepository dishJpaRepository;
    private final DishService dishService;
    private final ModelMapper modelMapper;
    @Override
    public List<DishTypeWithDishResponse> getAllDishTypes() {
        return dishTypeJpaRepository.findAll().stream().map(item -> {
            DishTypeWithDishResponse dishTypeResponse = new DishTypeWithDishResponse();
            dishTypeResponse.setId(item.getId());
            dishTypeResponse.setName(item.getName());
            List<DishNotIncludeType> dishes = dishJpaRepository.findDishesByDishType(item.getId(), PageRequest.of(0, 5));
            List<DishResponse> dishResponses = dishes.stream()
                    .map(dish -> {
                        DishResponse newDish = new DishResponse();
                        List<String> imageUrls = dishService.fetchImageUrls(dish.getName());
                newDish.setImageUrl(String.join(", ", imageUrls));    
            newDish.setId(dish.getId());
            newDish.setName(dish.getName());
            newDish.setDescription(dish.getDescription());
            newDish.setPrice(dish.getPrice());
            return newDish;
                    })
                    .toList();
            dishTypeResponse.setDishes(dishResponses);
            return dishTypeResponse;
        }).collect(Collectors.toList());
    }
    @Override
    public List<DishTypeOverview> getAllDishTypesWithTopThreeDishes() {
        
        List<DishTypeOverview> dishTypes = dishTypeJpaRepository.findAllOverviews().stream()
        .map(this::limitToTopThreeDishes)
        .collect(Collectors.toList());
        return dishTypes;
    }

    private DishTypeOverview limitToTopThreeDishes(DishTypeOverview dishType) {
        Pageable paging = PageRequest.of(0, 3);
        List<DishNotIncludeType> pagedResult = dishJpaRepository.findDishesByDishType(dishType.getId(), paging);
        System.out.println(pagedResult);
        List<FeaturedDish> limitedDishes = pagedResult.stream()
        .map(dish -> {
            FeaturedDish newDish = new FeaturedDish();
            List<String> imageUrls = dishService.fetchImageUrls(dish.getName());
            if (imageUrls.size() >= 3) {
                newDish.setImageUrl(String.join(", ", imageUrls));
            } else {
                newDish.setImageUrl("https://ik.imagekit.io/munchery/blog/tr:w-768/the-10-dishes-that-define-moroccan-cuisine.jpeg, https://giavivietan.com/wp-content/uploads/2020/01/VIANCO-Hinh-CHUP-T%C3%94-CA-RI-1-scaled.jpg, https://cms-prod.s3-sgn09.fptcloud.com/cach_nau_ca_ri_ga_tai_nha_bao_ngon_va_chuan_vi_an_hoai_khong_chan_1_c47c7657bc.jpg");
            }
            newDish.setId(dish.getId());
            newDish.setName(dish.getName());
            newDish.setDescription(dish.getDescription());
            newDish.setPrice(dish.getPrice());
            return newDish;
        })
        .collect(Collectors.toList());
        System.out.println(limitedDishes);
        DishTypeOverview dishTypeOverview = new DishTypeOverview();
        dishTypeOverview.setId(dishType.getId());
        dishTypeOverview.setName(dishType.getName());
        dishTypeOverview.setDishes(limitedDishes);
        return dishTypeOverview;
    }

    @Override
    public DishType create(DishTypeRequest dishTypeRequest) {
        DishType dishType = DishType.builder().name(dishTypeRequest.getName()).build();
        return dishTypeJpaRepository.save(dishType);
    }

    @Override
    public DishType update(long id, DishTypeRequest dishTypeRequest) throws Exception {
        return null;
    }

    @Override
    public DishType delete(long id) throws Exception {
        return null;
    }

    @Override
    public List<DishType> seed() {
        List<DishType> dishTypes = new ArrayList<>();
        Faker faker = new Faker();
        for (int i = 0; i < 9; i++) {
            DishType dishType = DishType.builder().name(faker.food().ingredient()).build();
            dishTypes.add(dishType);
            dishTypeJpaRepository.save(dishType);
        }
        return dishTypes;
    }
}
