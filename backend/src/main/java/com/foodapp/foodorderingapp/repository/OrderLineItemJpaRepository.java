package com.foodapp.foodorderingapp.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.foodapp.foodorderingapp.dto.dish.TopDishDTO;
import com.foodapp.foodorderingapp.entity.Order;
import com.foodapp.foodorderingapp.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.foodapp.foodorderingapp.entity.OrderLineItem;

@Repository
public interface OrderLineItemJpaRepository extends JpaRepository<OrderLineItem, Long> {
    @Query("""
    SELECT new com.foodapp.foodorderingapp.dto.dish.TopDishDTO(
        oli.dish.id,
        oli.dish.name,
        SUM(oli.quantity) AS totalQuantity,
        (SUM(oli.quantity) * 100.0 / (SELECT SUM(oli2.quantity) FROM OrderLineItem oli2 
                                      WHERE MONTH(oli2.order.createdAt) = :month 
                                      AND YEAR(oli2.order.createdAt) = :year
                                      AND oli2.order.restaurant = :restaurant
                                      ))
    ) 
    FROM OrderLineItem oli
    WHERE MONTH(oli.order.createdAt) = :month and oli.order.restaurant = :restaurant
    AND YEAR(oli.order.createdAt) = :year
    GROUP BY oli.dish.id, oli.dish.name
    ORDER BY totalQuantity DESC
    LIMIT 5
""")
    List<TopDishDTO> findTop5PopularProducts(@Param("restaurant") Restaurant restaurant, @Param("month") int month, @Param("year") int year);

}
