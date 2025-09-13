package com.sivalabs.bookstore.users.web;

import com.sivalabs.bookstore.users.domain.CreateUserCmd;
import com.sivalabs.bookstore.users.domain.Role;
import com.sivalabs.bookstore.users.domain.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
class UserController {
    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    String loginForm() {
        return "login";
    }

    @GetMapping("/registration")
    String registrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationForm("", "", ""));
        return "registration";
    }

    @PostMapping("/registration")
    String registerUser(
            @ModelAttribute("user") @Valid UserRegistrationForm userRegistrationForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        var cmd = new CreateUserCmd(
                userRegistrationForm.name(),
                userRegistrationForm.email(),
                userRegistrationForm.password(),
                Role.ROLE_USER);
        userService.createUser(cmd);
        return "redirect:/registration-success";
    }

    @GetMapping("/registration-success")
    String registrationSuccess() {
        return "registration-success";
    }

    public record UserRegistrationForm(
            @NotBlank(message = "Name is required") String name,
            @NotBlank(message = "Email is required") @Email(message = "Invalid email address") String email,
            @NotBlank(message = "Password is required") String password) {}
}
