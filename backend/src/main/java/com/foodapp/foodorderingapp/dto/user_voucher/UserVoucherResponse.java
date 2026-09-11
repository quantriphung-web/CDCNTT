package com.foodapp.foodorderingapp.dto.user_voucher;

import com.foodapp.foodorderingapp.entity.ProductDiscount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
public class UserVoucherResponse {
    private ProductDiscount productDiscount;
    private boolean status;
}
