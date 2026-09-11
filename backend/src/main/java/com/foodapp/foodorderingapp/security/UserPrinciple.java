package com.foodapp.foodorderingapp.security;

import com.foodapp.foodorderingapp.dto.auth.CreateUserRequest;
import com.foodapp.foodorderingapp.dto.auth.SignInRequest;
import com.foodapp.foodorderingapp.dto.auth.response.SignInResponse;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.exception.AuthException;
import com.foodapp.foodorderingapp.exception.UserExistException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor

public class UserPrinciple implements UserDetails {
    String username;
    String password;
    long userId;
    List<GrantedAuthority> authorities;

    public UserPrinciple(User user){
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.userId = user.getId();
        this.authorities = user.getRoles() != null ? user.getRoles().stream()
                .map(item -> new SimpleGrantedAuthority(item.getRole().getName()))
                .collect(Collectors.toList()) : new ArrayList<>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
