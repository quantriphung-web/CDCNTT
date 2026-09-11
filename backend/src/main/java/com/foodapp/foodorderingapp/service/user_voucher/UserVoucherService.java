package com.foodapp.foodorderingapp.service.user_voucher;

import com.foodapp.foodorderingapp.dto.user_voucher.UserVoucherRequest;
import com.foodapp.foodorderingapp.dto.user_voucher.UserVoucherResponse;
import com.foodapp.foodorderingapp.entity.Order;
import com.foodapp.foodorderingapp.entity.Voucher;
import com.foodapp.foodorderingapp.enumeration.VoucherStatus;

import java.util.List;

public interface UserVoucherService {
    Voucher receiveVoucher(UserVoucherRequest createUserVoucherRequest);
    Voucher useVoucher(Long voucherId, Order order);
    UserVoucherResponse tryApplyVoucher(Long voucherId);
    List<Voucher> findVouchersByStatusAndUserId(VoucherStatus status);
}
