package com.foodapp.foodorderingapp.service.cart;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.foodapp.foodorderingapp.converter.CartOptionConverter;
import com.foodapp.foodorderingapp.dto.cart.*;
import com.foodapp.foodorderingapp.dto.dish.DishResponse;
import com.foodapp.foodorderingapp.entity.*;
import com.foodapp.foodorderingapp.exception.DataNotFoundException;
import com.foodapp.foodorderingapp.repository.*;
import com.foodapp.foodorderingapp.repository.CartJpaRepository;
import com.foodapp.foodorderingapp.security.UserPrinciple;
import com.foodapp.foodorderingapp.service.dish.DishService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class CartServiceIml implements CartService {
    private final OptionItemJpaRepository optionItemJpaRepository;
    private final GroupOptionJpaRepository groupOptionJpaRepository;
    private final DishJpaRepository dishJpaRepository;
    private final CartJpaRepository cartJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CartOptionConverter cartOptionConverter;
    private final ModelMapper modelMapper;
    private final DishService dishService;

//    private Pair<List<CartItem_OptionItem>, BigDecimal> getItemOptions(CartItem_GroupOptionRequest item_group,
//            GroupOption groupOption) {
//        AtomicReference<BigDecimal> subTotal = new AtomicReference<>(BigDecimal.ZERO);
//        List<CartItem_OptionItem> item_options = item_group.getCartItem_optionItems().stream().map(item_option -> {
//            OptionItem optionItem = optionItemJpaRepository.findById(item_option.getOptionItemId()).orElseThrow(
//                    () -> new DataNotFoundException("Not found option item with " + item_option.getOptionItemId()));
//            if (!Objects.equals(optionItem.getGroupOption().getId(), groupOption.getId())) {
//                throw new IllegalArgumentException("Option item not valid" + item_option.getOptionItemId());
//            }
//            BigDecimal price = optionItem.getPrice().multiply(BigDecimal.valueOf(item_option.getQuantity()));
//            subTotal.updateAndGet(v -> v.add(price));
//            return CartItem_OptionItem.builder()
//                    .optionItem(optionItem)
//                    .quantity(item_option.getQuantity())
//                    .price(price)
//                    .build();
//        }).toList();

//        return Pair.of(item_options, subTotal.get());
//    }

//    private Pair<List<CartItem_GroupOptionList>, BigDecimal> getItemGroups(CartItemRequest itemRequest, CartItem item) {
//        AtomicReference<BigDecimal> itemOptionPrice = new AtomicReference<>(BigDecimal.ZERO);
//        List<CartItem_GroupOptionList> item_groups = itemRequest.getCartItemGroupOptionRequests().stream()
//                .map(item_group -> {
//                    GroupOption groupOption = groupOptionJpaRepository.findById(item_group.getGroupOptionId())
//                            .orElseThrow(() -> new DataNotFoundException(
//                                    "Not found group option with " + item_group.getGroupOptionId()));
//                    Pair<List<CartItem_OptionItem>, BigDecimal> res = getItemOptions(item_group, groupOption);
//                    System.out.println(res.getSecond());
//                    itemOptionPrice.updateAndGet(x -> x.add(res.getSecond()));
//
//                    CartItem_GroupOptionList cartItemGroupOption = CartItem_GroupOptionList.builder()
//                            .groupOption(groupOption)
//                            .cartItemOptions(res.getFirst())
//                            .groupOptionSubtotal(res.getSecond())
//                            .cartItem(item)
//                            .build();
//                    CartItem_GroupOptionList binh = cartGroupOptionRepository.save(cartItemGroupOption);
//                    res.getFirst().stream().forEach(option_item -> {
//                        option_item.setCartItem_groupOption(binh);
//                        cartOptionItemJpaRepository.save(option_item);
//                    });
//
//                    return CartItem_GroupOptionList.builder()
//                            .groupOption(groupOption)
//                            .cartItemOptions(res.getFirst())
//                            .groupOptionSubtotal(res.getSecond())
//                            .build();
//                }).toList();
//        System.out.println(itemOptionPrice.get());
//        return Pair.of(item_groups, itemOptionPrice.get());
//    }

    @Override
    public CartItemResponse upsertCartItem(CartItemRequest itemRequest) {
        Dish dish = dishJpaRepository.findById(itemRequest.getDishId())
                .orElseThrow(() -> new DataNotFoundException("Not found dish with id " + itemRequest.getDishId()));
        Long userId = ((UserPrinciple)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
        User user = userJpaRepository.findById(userId).orElseThrow(
                () -> new UsernameNotFoundException("Not found user")
        );
        CartItem item = cartJpaRepository.findByUserAndDish(user, dish);
        CartItem_GroupOptionList options = new CartItem_GroupOptionList();
        options.setSelectedOptions(itemRequest.getCartItemGroupOptionRequests().stream().map((option) ->
                CartItem_GroupOption.builder()
                        .groupOptionId(option.getGroupOptionId())
                        .selectedOptions(option.getSelectedOptions())
                        .build()
        ).toList());

        if(item == null) {
                 item = CartItem
                        .builder()
                        .dish(dish)
                        .user(user)
                        .options((options))
                         .quantity(itemRequest.getQuantity())
                        .build();
            item = cartJpaRepository.save(item);
        }
        else{
            item.setOptions((options));
            item.setQuantity(itemRequest.getQuantity());
        }
        CartItem cartItem = cartJpaRepository.save(item);
        CartItemResponse cartItemResponse = modelMapper.map(cartItem, CartItemResponse.class);
        DishResponse dishResponse = dishService.getDishById(cartItem.getDish().getId());
        cartItemResponse.setDish(dishResponse);
        return cartItemResponse;

    }

    @Override
    @Transactional
    public boolean removeFromCart(long cartId) {
        try {
            cartJpaRepository.deleteById(cartId);
            return true;
        } catch (Exception e) {
            throw new DataNotFoundException("Not found cart by id " + cartId);
        }
    }

    @Override
    public List<CartItem> getCartsByDish(CartOfDishRequest request) {
        return cartJpaRepository.findByDishAndUser(request.getDishId(), request.getUserId());
    }

    @Override
    public List<CartItemResponse> getCartByUser() {
        Long userId = ((UserPrinciple)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
        User user = userJpaRepository.findById(userId).orElseThrow(
                () -> new UsernameNotFoundException("Not found user")
        );
        List<CartItem> items = cartJpaRepository.findByUser(userId);
        return items.stream().map(item -> {
            CartItemResponse cartItemResponse = modelMapper.map(item, CartItemResponse.class);
            DishResponse dish = dishService.getDishById(item.getDish().getId());
            cartItemResponse.setDish(dish);
            return cartItemResponse;
        }).toList();
    }

    @Override
    public List<CartItem> getCartByRestaurant(long restaurantId, long userId) {
        return cartJpaRepository.findByRestaurant(userId, restaurantId);
    }

    @Override
    public CartItemResponse updateCart(int quantity, long id) {
        CartItem cartItem = cartJpaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("not found cart"));
        cartItem.setQuantity(quantity);
        if(quantity == 0) {
             cartJpaRepository.deleteById(id);
             return modelMapper.map(cartItem, CartItemResponse.class);
        }else{
          return  modelMapper.map(cartJpaRepository.save(cartItem), CartItemResponse.class);
        }
    }

    @Override
    public List<RestaurantCartResponse> getRestaurantOfCart(long userId) {
        List<CartItem> carts = cartJpaRepository.findByUser(userId);
        List<RestaurantCartResponse> restaurantCarts = new ArrayList<>();
        List<Long> listRestaurantIds = new ArrayList<>();
        carts.stream().forEach((cart -> {
            Long restaurantId = cart.getDish().getRestaurant().getId();
            RestaurantCartResponse data = new RestaurantCartResponse(cart.getDish().getRestaurant(), cart.getTotal(),
                    cart.getQuantity());
            if (listRestaurantIds.contains(restaurantId)) {
                RestaurantCartResponse res = restaurantCarts.stream()
                        .filter(item -> item.getRestaurant().getId().equals(restaurantId)).findFirst().orElseThrow();
                restaurantCarts.removeIf((item) -> item.getRestaurant().getId().equals(restaurantId));
                data.setQuantity(data.getQuantity() + res.getQuantity());
                data.setTotalPrice(data.getTotalPrice().add(res.getTotalPrice()));
                restaurantCarts.add(data);
            } else {
                listRestaurantIds.add(restaurantId);
                restaurantCarts.add(data);
            }
        }));
        return restaurantCarts;
    }

}
