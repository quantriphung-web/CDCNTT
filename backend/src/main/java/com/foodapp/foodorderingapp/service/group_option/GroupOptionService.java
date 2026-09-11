package com.foodapp.foodorderingapp.service.group_option;

import com.foodapp.foodorderingapp.dto.group_option.GroupOptionRequest;
import com.foodapp.foodorderingapp.dto.group_option.GroupOptionRequest;
import com.foodapp.foodorderingapp.entity.GroupOption;

import java.util.List;

public interface GroupOptionService {
    GroupOption addGroupOption(GroupOptionRequest groupOptionRequest);
    GroupOption updateGroupOption(long id, GroupOptionRequest groupOptionRequest);
    GroupOption deleteGroupOption(long id);
    List<GroupOption> getGroupOptionsByRestaurantId(long restaurantId);
    List<GroupOption> getGroupOptionsOfDish(long dishId);
}
