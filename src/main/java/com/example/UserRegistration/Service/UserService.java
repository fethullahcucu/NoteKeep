package com.example.UserRegistration.Service;

import java.util.List;

import com.example.UserRegistration.Dto.UserDto;
import com.example.UserRegistration.Model.User;
import com.example.UserRegistration.Service.UserService;

public interface UserService {

    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllUsers();
    
    void updateUserProfile(UserDto userDto);
}
