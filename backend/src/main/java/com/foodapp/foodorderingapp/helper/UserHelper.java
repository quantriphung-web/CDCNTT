package com.foodapp.foodorderingapp.helper;

import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.repository.UserJpaRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class UserHelper {
    private final UserJpaRepository userJpaRepository;
    public Optional<User> findCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userDetails.getUsername();
        return userJpaRepository.findUserByUsername(userDetails.getUsername());
    }
}
