package ru.ac.checkpointmanager.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.service.UserService;

@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/login")
    public String showLoginForm() {
        return "authentication/login";
    }

    @PostMapping("/login")
    public String processLogin(HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return "redirect:/welcome";
        } catch (AuthenticationException e) {
            return "redirect:/login?error";
        }
    }

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("user") UserAuthDTO user) {
        return "authentication/registration";
    }

    @PostMapping("/registration")
    public String performRegistration(@ModelAttribute("user") @Valid UserAuthDTO user,
                                      BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            return "/authentication/registration";

        userService.createUser(user);

        return "redirect:/authentication/login";
    }
}
