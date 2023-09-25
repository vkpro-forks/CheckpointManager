package ru.ac.checkpointmanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.service.UserService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

//    @GetMapping("/registration")
//    public String registrationPage(@ModelAttribute("user") UserAuthDTO user) {
//        return "auth/registration";
//    }
//
//    @PostMapping("/registration")
//    public String registerUser(@ModelAttribute("user") @Valid UserAuthDTO user, BindingResult result) {
//        if (result.hasErrors()) {
//            return "auth/registration";
//        }
//        userService.createUser(user);
//        return "redirect:/auth/login";
//    }
}
