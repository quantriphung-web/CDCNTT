package com.foodapp.foodorderingapp.service.user;

import com.foodapp.foodorderingapp.dto.address.CreateAddress;
import com.foodapp.foodorderingapp.dto.auth.CreateUserRequest;
import com.foodapp.foodorderingapp.dto.auth.NewUser;
import com.foodapp.foodorderingapp.dto.auth.SignInRequest;
import com.foodapp.foodorderingapp.dto.auth.UserResponse;
import com.foodapp.foodorderingapp.dto.auth.response.SignInResponse;
import com.foodapp.foodorderingapp.entity.Address;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.exception.AuthException;
import com.foodapp.foodorderingapp.exception.DataNotFoundException;
import com.foodapp.foodorderingapp.exception.UserExistException;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface UserService {
    UserResponse createNewUser(NewUser createUserRequest) throws UserExistException;
    SignInResponse signIn(SignInRequest signInRequest) throws AuthException;
    User getUserByUsername(String username) throws DataNotFoundException;
    User findCurrentUser();
    User findCurrentUser(Long id);
    User updateUser(CreateUserRequest createUserRequest) throws UserExistException;
}
