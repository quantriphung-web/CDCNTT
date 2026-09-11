package com.foodapp.foodorderingapp.dto.auth;

import lombok.*;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    private String username;
    private String password;
    private String fullname;
    private String avatarUrl;
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
}
