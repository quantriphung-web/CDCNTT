    package com.foodapp.foodorderingapp.entity;

    import com.fasterxml.jackson.annotation.JsonBackReference;
    import lombok.*;

    import jakarta.persistence.*;
    import java.math.BigDecimal;
    import java.util.List;
    import java.util.Objects;

import com.foodapp.foodorderingapp.enumeration.DeliveryStatus;
import com.foodapp.foodorderingapp.enumeration.OrderStatus;


    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "orders")
    @Entity
    public class Order extends BaseEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "CUSTOMER_ID")
        private User user;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "RESTAURANT_ID")
        @JsonBackReference
        private Restaurant restaurant;


        @Enumerated(EnumType.STRING)
        private OrderStatus orderStatus;

        @Enumerated(EnumType.STRING)
        private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

        private String failureMessages;
        private BigDecimal price;

        private String address;

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<OrderLineItem> items;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Order that = (Order) o;
            return id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        protected void onCreate() {
            super.onCreate();
            System.out.println("heheehee");
        }
    }
