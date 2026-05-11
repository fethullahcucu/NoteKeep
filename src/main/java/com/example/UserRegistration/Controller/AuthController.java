package com.example.UserRegistration.Controller;

import com.example.UserRegistration.Dto.UserDto;
import com.example.UserRegistration.Model.User;
import com.example.UserRegistration.Model.Role;
import com.example.UserRegistration.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;


    @GetMapping("/notekeep")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/notes";
        }
        return "notekeep";
    }


    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/notekeep";
    }


    
    @GetMapping("/register")
    public String showRegistrationForm(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/notes";
        }
        UserDto user = new UserDto();
        model.addAttribute("user", user);
        return "register";
    }

    
    @PostMapping("/register/save")
    public String registration(@ModelAttribute("user") UserDto userDto,
                               BindingResult result,
                               Model model) {
        User existingUser = userService.findUserByEmail(userDto.getEmail());

        if (existingUser != null && existingUser.getEmail() != null && !existingUser.getEmail().isEmpty()) {
            result.rejectValue("email", null,
                    "There is already an account registered with the same email");
        }

        if (result.hasErrors()) {
            model.addAttribute("user", userDto);
            return "register";
        }

        userService.saveUser(userDto);
        return "redirect:/register?success";
    }

    
    @GetMapping("/admin/users")
    public String adminUsers(Model model, Authentication authentication) {
        User currentUser = userService.findUserByEmail(authentication.getName());
        if (currentUser == null || !isAdmin(currentUser)) {
            return "redirect:/notes";
        }

        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin-users";
    }

    @GetMapping("/admin/users/{userId}/edit")
    public String adminEditUser(@PathVariable String userId, Model model, Authentication authentication) {
        User currentUser = userService.findUserByEmail(authentication.getName());
        if (currentUser == null || !isAdmin(currentUser)) {
            return "redirect:/notes";
        }

        User user = userService.findUserById(userId);
        if (user == null) {
            return "redirect:/admin/users";
        }

        model.addAttribute("user", user);
        return "admin-edit-user";
    }

    @PostMapping("/admin/users/{userId}/update")
    public String adminUpdateUser(@PathVariable String userId,
                                   @RequestParam String firstName,
                                   @RequestParam String lastName,
                                   @RequestParam String email,
                                   Authentication authentication) {
        User currentUser = userService.findUserByEmail(authentication.getName());
        if (currentUser == null || !isAdmin(currentUser)) {
            return "redirect:/notes";
        }

        userService.updateUserById(userId, firstName, lastName, email);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{userId}/delete")
    public String adminDeleteUser(@PathVariable String userId,
                                   Authentication authentication) {
        User currentUser = userService.findUserByEmail(authentication.getName());
        if (currentUser == null || !isAdmin(currentUser)) {
            return "redirect:/notes";
        }

        userService.deleteUserById(userId);
        return "redirect:/admin/users";
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRoles() != null && user.getRoles().contains(Role.ROLE_ADMIN);
    }
}
