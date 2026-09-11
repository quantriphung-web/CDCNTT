package com.foodapp.foodorderingapp.controller;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RestaurantDateRangeRequest {
    private Date startDate;
    private Date endDate;
    private Long restaurantId;
}
