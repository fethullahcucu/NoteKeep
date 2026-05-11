package com.example.UserRegistration.Service;


import com.example.UserRegistration.Dto.UserDto;
import com.example.UserRegistration.Model.Role;
import com.example.UserRegistration.Model.User;
import com.example.UserRegistration.Repository.UserRepository;
import com.example.NoteKeep.Repository.NoteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           NoteRepository noteRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        // Assign default role(s) using enum
        user.setRoles(Set.of(Role.ROLE_USER));
        userRepository.save(user);
    }
    
    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User findUserById(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }

    @Override
    public void updateUserProfile(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail()).orElse(null);
        if (user == null) return;

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        userRepository.save(user);
    }

    @Override
    public void updateUserById(String userId, String firstName, String lastName, String email) {
        if (userId == null || userId.isBlank()) return;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName);
        }
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        userRepository.save(user);
    }

    @Override
    public void deleteUserById(String userId) {
        if (userId == null || userId.isBlank()) return;

        noteRepository.deleteAll(noteRepository.findByUserId(userId));
        userRepository.deleteById(userId);
    }

}
