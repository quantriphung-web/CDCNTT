package com.foodapp.foodorderingapp.security;

import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceIml implements UserDetailsService {
    private final UserJpaRepository userJpaRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = (userJpaRepository.findUserByUsername(username));
        System.out.println("user" + user);
        if(user.isEmpty()) throw new UsernameNotFoundException("Not found user");
        else return new UserPrinciple(user.get());
    }
}
