package com.foodapp.foodorderingapp.controller;

import com.foodapp.foodorderingapp.dto.order.OrderRequest;
import com.foodapp.foodorderingapp.dto.order.OrderResponse;
import com.foodapp.foodorderingapp.entity.Dish;
import com.foodapp.foodorderingapp.entity.Order;
import com.foodapp.foodorderingapp.enumeration.OrderStatus;
import com.foodapp.foodorderingapp.security.UserPrinciple;
import com.foodapp.foodorderingapp.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map.Entry;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("/user")
    public ResponseEntity<List<Order>> getMyOrder() {
        UserPrinciple userPrinciple = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(orderService.getByUser(userPrinciple.getUserId()));
    }

    @GetMapping("/statistic/{restaurantId}")
    public ResponseEntity<List<Dish>> getStatistic(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(orderService.getHotOrders(restaurantId));
    }

    @GetMapping("/restaurant/{id}")
    public ResponseEntity<List<OrderResponse>> getOrderByRestaurantId(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getByRestaurant(id));
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.createNewOrder(orderRequest));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrderStatus(@Valid @RequestParam OrderStatus orderStatus, @PathVariable Long id){
        return ResponseEntity.ok( orderService.updateOrderStatus(id, orderStatus));
    }
}
