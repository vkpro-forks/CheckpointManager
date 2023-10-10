package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.security.AuthenticationRequest;
import ru.ac.checkpointmanager.security.AuthenticationResponse;
import ru.ac.checkpointmanager.security.AuthenticationService;

import java.io.IOException;

import static ru.ac.checkpointmanager.utils.ErrorUtils.errorsList;

@RestController
@RequiredArgsConstructor
@RequestMapping("chpman/authentication")
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT"
        )
})
public class AuthController {

    private final AuthenticationService service;

    @PostMapping("/registration")
    @Operation(summary = "registration")
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "login")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "refresh token")
    @SecurityRequirement(name = "bearerAuth")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        service.refreshToken(request, response);
    }
}
