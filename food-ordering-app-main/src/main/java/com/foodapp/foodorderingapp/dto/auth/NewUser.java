package com.foodapp.foodorderingapp.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NewUser {
    private String username;
    private String password;
    private String fullname;
    private String phoneNumber;
    @Builder.Default
    private Integer age = 0;
    @Builder.Default
    private Integer weight = 0;
    @Builder.Default
    private Integer height = 0;
    @Builder.Default
    private String activity = "";
    @Builder.Default
    private Integer mealPerDay = 3;
    @Builder.Default
    private String weightLoss = "";
    @Builder.Default
    private String gender = "male";
    private String avatarUrl;
    private String connectedAccountId;
}
