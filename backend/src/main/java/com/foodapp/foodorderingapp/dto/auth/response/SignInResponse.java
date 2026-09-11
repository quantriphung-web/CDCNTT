package com.foodapp.foodorderingapp.dto.auth.response;

import com.foodapp.foodorderingapp.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignInResponse {
    String token;
    User user;
}
