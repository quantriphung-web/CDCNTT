package com.foodapp.foodorderingapp.dto.review;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreateReview {
    private long userId;
    private long restaurantId;
    private String comment;
    private int rate;
}
