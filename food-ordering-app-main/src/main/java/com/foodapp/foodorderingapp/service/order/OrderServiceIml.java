package com.foodapp.foodorderingapp.service.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.foodapp.foodorderingapp.dto.order.*;
import com.foodapp.foodorderingapp.entity.*;
import com.foodapp.foodorderingapp.enumeration.DeliveryStatus;
import com.foodapp.foodorderingapp.enumeration.DiscountType;
import com.foodapp.foodorderingapp.enumeration.OrderStatus;
import com.foodapp.foodorderingapp.exception.DataNotFoundException;
import com.foodapp.foodorderingapp.repository.*;
import com.foodapp.foodorderingapp.security.UserPrinciple;

import com.foodapp.foodorderingapp.service.cart.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceIml implements OrderService {
        private final OrderJpaRepository orderJpaRepository;
        private final OptionItemJpaRepository optionItemJpaRepository;
        private final GroupOptionJpaRepository groupOptionJpaRepository;
        private final DishJpaRepository dishJpaRepository;
        private final UserJpaRepository userJpaRepository;
        private final OrderLineItemJpaRepository orderLineItemJpaRepository;
        private final VoucherJpaRepository voucherJpaRepository;
        private final RestaurantJpaRepository restaurantJpaRepository;
        private final VoucherApplicationJpaRepository voucherApplicationJpaRepository;
        private final CartJpaRepository cartJpaRepository;
        private final ModelMapper modelMapper;
        private final CartService cartService;

    private Pair<List<OrderLineItem_OptionItem>, BigDecimal> getItemOptions(
            CartItem_GroupOption cartItem_groupOption,
                    GroupOption groupOption) {
            AtomicReference<BigDecimal> subTotal = new AtomicReference<>(BigDecimal.ZERO);
            List<OrderLineItem_OptionItem> lineOptions = cartItem_groupOption.getSelectedOptions().stream()
                .map(option -> {
                        OptionItem optionItem = optionItemJpaRepository
                                        .findById(option).orElseThrow(
                                                        () -> new DataNotFoundException(
                                                                        "Not found option item with "
                                                                                        + option));
                        if (!Objects.equals(optionItem.getGroupOption().getId(), groupOption.getId())) {
                                throw new IllegalArgumentException("Option item not valid"
                                                + option);
                        }
                    subTotal.updateAndGet(v -> v.add(optionItem.getPrice()));
                    return OrderLineItem_OptionItem.builder()
                            .optionId(optionItem.getId())
                            .optionName(optionItem.getName())
                            .price(optionItem.getPrice())
                            .build();
                }).toList();
            return Pair.of(lineOptions, subTotal.get());
    }

    private OrderLineItem_GroupOptionList getItemGroups(CartItem_GroupOptionList cart_groups,
                                                                                OrderLineItem item) {
            AtomicReference<BigDecimal> price = new AtomicReference<>(BigDecimal.ZERO);
            List<OrderLineItem_GroupOption> groups = cart_groups.getSelectedOptions().stream()
                .map(group -> {
                        GroupOption groupOption = groupOptionJpaRepository
                            .findById(group.getGroupOptionId())
                            .orElseThrow(() -> new DataNotFoundException(
                                            "Not found group option with " + group
                                                            .getGroupOptionId()));
                        Pair<List<OrderLineItem_OptionItem>, BigDecimal> res = getItemOptions(
                                        group, groupOption);
                        price.updateAndGet(x -> x.add(res.getSecond()));
                        return OrderLineItem_GroupOption
                                .builder()
                                .groupOptionId(groupOption.getId())
                                .selectedOptions(res.getFirst())
                                .groupOptionName(groupOption.getName())
                                .build();
                }).toList();
            return OrderLineItem_GroupOptionList.builder()
                    .groups(groups)
                    .price(price.get())
                    .build();
    }

    @Transactional
    @Override
    public Order createNewOrder(OrderRequest orderRequest) {
            UserPrinciple currentUser = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication()
                            .getPrincipal();
            List<Long> voucherIds = orderRequest.getVoucherIds();
            List<Voucher> vouchers = new ArrayList<Voucher>();
            Order order = Order.builder()
                            .user(userJpaRepository.findById(currentUser.getUserId())
                                            .orElseThrow(() -> new DataNotFoundException("Not found user")))
                            .orderStatus(orderRequest.getOrderStatus())
                            .deliveryStatus(DeliveryStatus.PENDING)
                            .address(orderRequest.getAddress())

                            .build();
//        @Transactional
//        @Override
//        public Order createNewOrder(OrderRequest orderRequest) {
//                UserPrinciple currentUser = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication()
//                                .getPrincipal();
//                List<Long> voucherIds = orderRequest.getVoucherIds();
//                List<Voucher> vouchers = new ArrayList<Voucher>();
//                Order order = Order.builder()
//                                .user(userJpaRepository.findById(currentUser.getUserId())
//                                                .orElseThrow(() -> new DataNotFoundException("Not found user")))
//                                .orderStatus(orderRequest.getOrderStatus())
//                                .deliveryStatus(DeliveryStatus.PENDING)
//                                .address(orderRequest.getAddress())
//
//                                .build();

            Order orderData = orderJpaRepository.save(order);
            voucherIds.forEach(voucherId -> {
                    Voucher voucher = voucherJpaRepository.findById(voucherId)
                                    .orElseThrow(() -> new DataNotFoundException(
                                                    "Not found voucher with id " + voucherId));
                    voucher.setRemainingUsage(Math.max(voucher.getRemainingUsage() - 1, 0));
                    voucherJpaRepository.save(voucher);
                    vouchers.add(voucher);
                    VoucherApplication voucherApplication = new VoucherApplication();
                    voucherApplication.setOrder(order);
                    voucherApplication.setVoucher(voucher);
                    voucherApplicationJpaRepository.save(voucherApplication);
            });
            AtomicReference<BigDecimal> totalPrice = new AtomicReference<>(BigDecimal.ZERO);
            List<OrderLineItem> orderLineItems = orderRequest.getCartItemIds().stream().map(id -> {
                    CartItem cartItem = cartJpaRepository.findById(id).orElseThrow(
                            () -> new DataNotFoundException("Not found cart with id" + id)
                    );
                    OrderLineItem orderLineItem = OrderLineItem
                                    .builder()
                                    .dish(cartItem.getDish())
                                    .quantity(cartItem.getQuantity())
                                    .order(orderData)
                                    .build();
                    OrderLineItem item = orderLineItemJpaRepository.save(orderLineItem);
                    OrderLineItem_GroupOptionList itemGroups = getItemGroups(cartItem.getOptions(), item);
                     orderLineItem.setOptions(itemGroups);
                    BigDecimal subTotal = (itemGroups.getPrice())
                                    .multiply(BigDecimal.valueOf(cartItem.getQuantity())).add(cartItem.getDish().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
                    totalPrice.updateAndGet(x -> x.add(subTotal)
                                    );
                    item.setSubTotal(subTotal);
                    return orderLineItemJpaRepository.save(item);
            }).collect(Collectors.toList());;
            vouchers.forEach(voucher -> {
                    ProductDiscount discount = voucher.getProductDiscount();
                    BigDecimal discountValue = BigDecimal.valueOf(discount.getDiscountValue());
                    if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
                            totalPrice.updateAndGet(x -> x.multiply(BigDecimal.valueOf(1)
                                            .subtract(discountValue.divide(BigDecimal.valueOf(100), 2,
                                                            RoundingMode.HALF_UP))));
                    } else {
                            totalPrice.updateAndGet(x -> x.subtract(discountValue));
                    }
            });
            orderData.setItems(orderLineItems);
            orderData.setPrice(totalPrice.get());
            orderRequest.getCartItemIds().forEach(id -> cartService.updateCart(0, id));
            return orderJpaRepository.save(orderData);
    }

    @Override
    public Optional<Order> findById(String orderId) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public Optional<Order> findByTrackingId(String trackingId) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'findByTrackingId'");
    }

    @Override
    public List<Order> getByUser(Long userId) {
            return orderJpaRepository.findByUser(userId);
    }

    @Override
    public List<OrderResponse> getByRestaurant (Long restaurantId) {
            return orderJpaRepository.findOrdersByRestaurantId(restaurantId).stream().map(
                    item -> modelMapper.map(item, OrderResponse.class)
            ).toList();
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus orderStatus) {
        Order order = orderJpaRepository.findById(orderId).orElseThrow(()-> new DataNotFoundException("Not found order"));
        order.setOrderStatus(orderStatus);
        return orderJpaRepository.save(order);
    }
//        @Override
//        public List<Order> getByRestaurantAndOrderStatus(Long restaurantId, OrderStatus orderStatus) {
//                return orderJpaRepository.findOrdersByRestaurantIdAndOrderStatus(restaurantId, orderStatus);
//        }

        @Override
public List<Dish> getHotOrders(Long restaurantId) {
    Restaurant restaurant = restaurantJpaRepository.findById(restaurantId)
        .orElseThrow(() -> new DataNotFoundException(
            "Not found restaurant with id " + restaurantId));
    List<OrderLineItem> orderLineItems = orderLineItemJpaRepository.findAll();
    Map<Dish, Long> dishCounts = orderLineItems.stream()
        .filter(item -> item.getDish().getRestaurant().equals(restaurant))
        .collect(Collectors.groupingBy(OrderLineItem::getDish, Collectors.counting()));


        List<Dish> sortedDishes = dishCounts.entrySet().stream()
        .sorted(Map.Entry.<Dish, Long>comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    return sortedDishes;
}
}
