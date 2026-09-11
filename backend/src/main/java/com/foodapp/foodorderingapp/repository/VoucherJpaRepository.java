package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.ProductDiscount;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.entity.Voucher;
import com.foodapp.foodorderingapp.enumeration.VoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface VoucherJpaRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findAllByStatusAndUserId(VoucherStatus status, Long userId);
    Optional<Voucher> findFirstByProductDiscountAndUser(ProductDiscount productDiscount, User user);
    Optional<Voucher> findFirstVoucherByProductDiscountAndUserOrderByRemainingUsageDesc(ProductDiscount productDiscount, User user);
}
