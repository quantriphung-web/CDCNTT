package com.foodapp.foodorderingapp.service.statistic;

import com.foodapp.foodorderingapp.controller.RestaurantDateRangeRequest;
import com.foodapp.foodorderingapp.dto.dish.TopDishDTO;
import com.foodapp.foodorderingapp.dto.statistic.StatisticModelRes;
import com.foodapp.foodorderingapp.entity.Restaurant;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface StatisticService {
    List<Map<String, Object>> getCategoryPercentages(Long restaurantId);
    StatisticModelRes getOrderStatistic(Long restaurantId);
    List<ChartData> generateChartData(RestaurantDateRangeRequest model);
     List<TopDishDTO> getTopDish(Long restaurantId);
}
