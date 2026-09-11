package com.foodapp.foodorderingapp.service.product_discount;

import com.foodapp.foodorderingapp.dto.product_discount.CreateProductDiscountRequest;
import com.foodapp.foodorderingapp.dto.product_discount.DiscountResponse;
import com.foodapp.foodorderingapp.dto.product_discount.UpdateProductDiscountRequest;
import com.foodapp.foodorderingapp.entity.ProductDiscount;
import com.foodapp.foodorderingapp.entity.Restaurant;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.entity.Voucher;
import com.foodapp.foodorderingapp.helper.UserHelper;
import com.foodapp.foodorderingapp.repository.ProductDiscountJpaRepository;
import com.foodapp.foodorderingapp.repository.RestaurantJpaRepository;
import com.foodapp.foodorderingapp.repository.VoucherJpaRepository;
import com.foodapp.foodorderingapp.service.user_voucher.UserVoucherService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductDiscountServiceImpl implements ProductDiscountService{
    private final ProductDiscountJpaRepository productDiscountJpaRepository;
    private final ModelMapper mapper;
    private final RestaurantJpaRepository restaurantJpaRepository;
    private final VoucherJpaRepository voucherJpaRepository;
    private final UserHelper userHelper;
    @Override
    public DiscountResponse createProductDiscount(CreateProductDiscountRequest productDiscountRequest) {
        Restaurant restaurant = restaurantJpaRepository.findById(productDiscountRequest.getRestaurantId()).orElseThrow(() -> new RuntimeException("Dish not found"));
        ProductDiscount productDiscount = mapper.map(productDiscountRequest, ProductDiscount.class);
        productDiscount.setRestaurant(restaurant);
        return mapper.map( productDiscountJpaRepository.save(productDiscount), DiscountResponse.class);
    }
    @Override
    public DiscountResponse updateProductDiscount(UpdateProductDiscountRequest productDiscountRequest) {
        ProductDiscount discount = productDiscountJpaRepository.findById(productDiscountRequest.getId()).orElseThrow(
                () ->  new RuntimeException("Not found discount")
        );
        mapper.map(productDiscountRequest, discount);
        return mapper.map( productDiscountJpaRepository.save(discount), DiscountResponse.class);
    }

    @Override
    public DiscountResponse getProductDiscount(Long id) {
        return mapper.map( productDiscountJpaRepository.findById(id).orElseThrow(() -> new RuntimeException("Product discount not found")), DiscountResponse.class);
    }
    private DiscountResponse setVoucherInformation(ProductDiscount productDiscount){
        DiscountResponse discountResponse = mapper.map(productDiscount, DiscountResponse.class);
        User user = userHelper.findCurrentUser().orElseThrow( () -> new IllegalArgumentException("Not found user"));
        Optional<Voucher> voucher = voucherJpaRepository.findFirstVoucherByProductDiscountAndUserOrderByRemainingUsageDesc(productDiscount, user);
        if(voucher.isPresent()){
            discountResponse.setRemainingUsed(voucher.get().getRemainingUsage());
        }
        else discountResponse.setRemainingUsed(0);
        return  discountResponse;
    }
    @Override
    public List<DiscountResponse> getProductDiscountByRestaurant(Long restaurantId) {
        return productDiscountJpaRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(this::setVoucherInformation)
                .collect(Collectors.toList());
        }

}
