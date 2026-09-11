package com.foodapp.foodorderingapp.dto.user_voucher;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserVoucherRequest {
    private String code;
    private Long productDiscountId;
}
