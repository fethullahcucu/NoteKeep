package com.example.UserRegistration.Controller;


import com.example.UserRegistration.Dto.UserDto;
import com.example.UserRegistration.Model.User;
import com.example.UserRegistration.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
    public class ProfileController {

        @Autowired
        private UserService userService;

        @GetMapping("/profile/edit")
        public String showEditForm(Model model) {
            // Get current user's email from Spring Security context
            Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userService.findUserByEmail(email);
            UserDto userDto = new UserDto();
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setEmail(user.getEmail());
            model.addAttribute("user", userDto);
            return "editProfile";
        }

        @PostMapping("/profile/update")
        public String updateProfile(@ModelAttribute("user") UserDto userDto) {
            userService.updateUserProfile(userDto);
            return "redirect:/profile/edit?success";
        }
    }