package ru.ac.checkpointmanager.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.security.AuthenticationRequest;
import ru.ac.checkpointmanager.security.AuthenticationResponse;
import ru.ac.checkpointmanager.security.AuthenticationService;

import java.io.IOException;

import static ru.ac.checkpointmanager.utils.ErrorUtils.errorsList;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authentication")
public class AuthController {

    private final AuthenticationService service;

    @PostMapping("/registration")
    public ResponseEntity<?> register(@RequestBody @Valid UserAuthDTO user,
                                      BindingResult result) {

        if (result.hasErrors())
            return new ResponseEntity<>(errorsList(result), HttpStatus.BAD_REQUEST);

        try {
            return new ResponseEntity<>(service.createUser(user), HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        service.refreshToken(request, response);
    }
}
