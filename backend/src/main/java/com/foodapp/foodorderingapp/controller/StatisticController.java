package com.foodapp.foodorderingapp.controller;

import com.foodapp.foodorderingapp.dto.dish.TopDishDTO;
import com.foodapp.foodorderingapp.dto.statistic.StatisticModelRes;
import com.foodapp.foodorderingapp.service.statistic.ChartData;
import com.foodapp.foodorderingapp.service.statistic.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping("/categories_percent/{restaurantId}")
    public List<Map<String, Object>> getCategoryPercentages(@PathVariable Long restaurantId){
        return statisticService.getCategoryPercentages(restaurantId);
    }
    @GetMapping("/order/{restaurantId}")
    public StatisticModelRes getOrderStatistic(@PathVariable Long restaurantId){
        return statisticService.getOrderStatistic(restaurantId);
    }
    @GetMapping("/top_dish/{restaurantId}")
    public List<TopDishDTO> getTopDishStatistic(@PathVariable Long restaurantId){
        return statisticService.getTopDish(restaurantId);
    }
    @PostMapping("/dateRange")
    ResponseEntity<List<ChartData>> getStockReportDateRange(@RequestBody RestaurantDateRangeRequest model){
        return ResponseEntity.ok(statisticService.generateChartData(model));
    }
}
