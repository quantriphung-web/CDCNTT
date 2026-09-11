package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.Voucher;
import com.foodapp.foodorderingapp.entity.VoucherApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherApplicationJpaRepository extends JpaRepository<VoucherApplication, Long> {

}
