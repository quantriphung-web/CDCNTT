package com.foodapp.foodorderingapp.controller;

import com.foodapp.foodorderingapp.dto.product_discount.CreateProductDiscountRequest;
import com.foodapp.foodorderingapp.dto.product_discount.DiscountResponse;
import com.foodapp.foodorderingapp.dto.product_discount.UpdateProductDiscountRequest;
import com.foodapp.foodorderingapp.entity.ProductDiscount;
import com.foodapp.foodorderingapp.service.product_discount.ProductDiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class ProductDiscountController {
    private final ProductDiscountService productDiscountService;
    @GetMapping("/{id}")
    public ResponseEntity<DiscountResponse> getProductDiscount(@PathVariable Long id) {
        return ResponseEntity.ok(productDiscountService.getProductDiscount(id));
    }
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<DiscountResponse>> getProductDiscountByRestaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(productDiscountService.getProductDiscountByRestaurant(restaurantId));
    }
    @PostMapping
    public ResponseEntity<DiscountResponse> createProductDiscount(@Valid @RequestBody CreateProductDiscountRequest productDiscountRequest) {
        return ResponseEntity.ok(productDiscountService.createProductDiscount(productDiscountRequest));
    }
    @PutMapping
    public ResponseEntity<DiscountResponse> updateProductDiscount(@Valid @RequestBody UpdateProductDiscountRequest productDiscountRequest){
        return ResponseEntity.ok(productDiscountService.updateProductDiscount(productDiscountRequest));
    }
}
