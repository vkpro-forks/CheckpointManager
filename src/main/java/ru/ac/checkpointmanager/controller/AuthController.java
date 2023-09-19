package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.service.UserService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("user") UserAuthDTO user) {
        return "auth/registration";
    }

    @PostMapping("/registration")
    public String registerUser(@ModelAttribute("user") @Valid UserAuthDTO user, BindingResult result) {
        if (result.hasErrors()) {
            return "auth/registration";
        }
        userService.createUser(user);
        return "redirect:/auth/login";
    }
}
