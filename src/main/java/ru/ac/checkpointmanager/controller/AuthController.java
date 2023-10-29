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
import ru.ac.checkpointmanager.dto.AuthenticationRequest;
import ru.ac.checkpointmanager.dto.AuthenticationResponse;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.service.AuthenticationService;

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
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/registration")
    @Operation(summary = "registration")
    public ResponseEntity<?> register(@RequestBody @Valid UserAuthDTO user,
                                      BindingResult result) {

        if (result.hasErrors()) {
            return new ResponseEntity<>(errorsList(result), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(authenticationService.preRegister(user), HttpStatus.OK);
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> confirmRegistration(@RequestParam("token") String token) {
        authenticationService.confirmRegistration(token);
        return ResponseEntity.ok("Регистрация успешно подтверждена, можете войти в аккаунт, " +
                "используя адрес электронной почты и пароль, указанные при регистрации");
    }

    @PostMapping("/login")
    @Operation(summary = "login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "refresh token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authenticationService.refreshToken(request, response);
    }
}
