package com.example.UserRegistration.Dto;

import com.example.UserRegistration.Model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Set<Role> roles;
    
}
