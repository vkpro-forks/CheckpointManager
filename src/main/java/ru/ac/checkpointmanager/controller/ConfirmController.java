package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.user.AuthResponseDTO;
import ru.ac.checkpointmanager.service.auth.AuthenticationService;
import ru.ac.checkpointmanager.service.user.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/confirm")
@Tag(name = "Подтверждение данных", description = "Api, на которые переходят ссылки из писем")
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT"
        )
})
@SecurityRequirement(name = "bearerAuth")
public class ConfirmController {

    private final AuthenticationService authenticationService;
    private final UserService userService;


    @Operation(summary = "Подтверждение регистрации по ссылке")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "NO CONTENT: Регистрация подтверждена"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: Ссылка подтверждения недействительна или истек срок действия"
            )
    })
    @GetMapping("/registration")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmRegistration(
            @Parameter(description = "Токен из письма для подтверждения")
            @RequestParam("token") String token) {
        authenticationService.confirmRegistration(token);
    }

    @Operation(summary = "Подтверждение новой электронной почты по ссылке")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: Почта подтверждена",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: Ссылка подтверждения недействительна или истек срок действия"
            )
    })
    @GetMapping("/email")
    public AuthResponseDTO confirmEmail(@Parameter(description = "Токен из письма для подтверждения")
                                               @RequestParam("token") String token) {
        return userService.confirmEmail(token);
    }
}