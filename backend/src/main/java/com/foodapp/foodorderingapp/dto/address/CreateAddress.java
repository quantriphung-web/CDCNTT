package com.foodapp.foodorderingapp.dto.address;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateAddress {
    private long userId;
    private String address;
    private int provinceCode;
    private int districtCode;
    private int wardCode;
}
