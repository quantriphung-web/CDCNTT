package com.foodapp.foodorderingapp.service.product_discount;

import com.foodapp.foodorderingapp.dto.product_discount.CreateProductDiscountRequest;
import com.foodapp.foodorderingapp.dto.product_discount.DiscountResponse;
import com.foodapp.foodorderingapp.dto.product_discount.UpdateProductDiscountRequest;
import com.foodapp.foodorderingapp.entity.ProductDiscount;

import java.util.List;

public interface ProductDiscountService {
    DiscountResponse createProductDiscount(CreateProductDiscountRequest productDiscountRequest);
    DiscountResponse updateProductDiscount(UpdateProductDiscountRequest productDiscountRequest);
    DiscountResponse getProductDiscount(Long id);
    List<DiscountResponse> getProductDiscountByRestaurant(Long restaurantId);
}
