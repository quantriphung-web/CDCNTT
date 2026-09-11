package com.foodapp.foodorderingapp.dto.statistic;

import lombok.Data;

@Data
public class StatisticModelRes {
//    private int indboundNumber;
//    private int inboundNeededNumber;
//    private double stockPercent;
//    private double inboundPercent;
//    private double inboundNeededPercent;
    private String totalPricePercentChange;
    private int totalQuantity;
    private int cancelQuantity;
    private double totalPrice;

    private int totalQuantity1;
    private int cancelQuantity1;
    private double totalPrice1;

    private String totalQuantityPercentChange;
    private String cancelQuantityPercentChange;
}
