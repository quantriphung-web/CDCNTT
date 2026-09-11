package com.foodapp.foodorderingapp.service.group_option;

import com.foodapp.foodorderingapp.dto.group_option.GroupOptionRequest;
import com.foodapp.foodorderingapp.dto.group_option.OptionItemRequest;
import com.foodapp.foodorderingapp.entity.Category;
import com.foodapp.foodorderingapp.entity.GroupOption;
import com.foodapp.foodorderingapp.entity.OptionItem;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.repository.CategoryJpaRepository;
import com.foodapp.foodorderingapp.repository.Dish_GroupOptionJpaRepository;
import com.foodapp.foodorderingapp.repository.GroupOptionJpaRepository;
import com.foodapp.foodorderingapp.repository.RestaurantJpaRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
public class GroupOptionServiceImpl implements GroupOptionService {
    private final CategoryJpaRepository categoryJpaRepository;
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final GroupOptionJpaRepository groupOptionJpaRepository;
    private final Dish_GroupOptionJpaRepository dish_groupOptionJpaRepository;
    @Override
    public GroupOption addGroupOption(GroupOptionRequest groupOptionRequest) {
        Restaurant existingRestaurant = restaurantJpaRepository
                .findById(groupOptionRequest.getRestaurantId())
                .orElseThrow(() ->
                new IllegalArgumentException(
                        "Cannot find restaurant with id: "+ groupOptionRequest.getRestaurantId()));
        //build group option information
        GroupOption newGroupOption = GroupOption.builder()
                .name(groupOptionRequest.getName())
                .minimum(groupOptionRequest.getMinimum())
                .maximum(groupOptionRequest.getMaximum())
                .isOptional(groupOptionRequest.isOptional())
                .restaurant(existingRestaurant)
                .build();
        //build option item list
        List<OptionItem> optionItems = groupOptionRequest.getOptionItemRequests().stream().map(optionItem -> OptionItem.builder().groupOption(newGroupOption)
                .price(optionItem.getPrice())
                .name(optionItem.getName())
                .description(optionItem.getDescription())
                .build()
        ).toList();
        //set to group option
        newGroupOption.setOptionItems(optionItems);
        return groupOptionJpaRepository.save(newGroupOption);
    }

    @Override
    public GroupOption updateGroupOption(long id, GroupOptionRequest groupOptionRequest) {
        return null;
    }

    @Override
    public GroupOption deleteGroupOption(long id) {
        return null;
    }

    @Override
    public List<GroupOption> getGroupOptionsByRestaurantId(long restaurantId) {
        return groupOptionJpaRepository.findGroupOptionByRestaurantId(restaurantId);
    }

    @Override
    public List<GroupOption> getGroupOptionsOfDish(long dishId) {
        return dish_groupOptionJpaRepository.findByDishId(dishId).stream()
                .map(item -> item.getDish_groupOptionId().getGroupOption())
                .toList();
    }
}
