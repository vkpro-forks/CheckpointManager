package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.service.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/welcome")
    public String greeting() {
        return "welcome";
    }

    @GetMapping("/authentication/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("user") UserAuthDTO user) {
        return "registration";
    }

    @PostMapping(
            path = "/registration",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String performRegistration(@ModelAttribute("user") @Valid UserAuthDTO user,
                                      BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            return "registration";

        userService.createUser(user);

        return "redirect:/swagger-ui.html";
    }
}
