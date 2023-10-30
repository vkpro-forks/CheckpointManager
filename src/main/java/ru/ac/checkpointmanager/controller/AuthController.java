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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.AuthenticationRequest;
import ru.ac.checkpointmanager.dto.AuthenticationResponse;
import ru.ac.checkpointmanager.dto.UserAuthDTO;
import ru.ac.checkpointmanager.service.AuthenticationService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.io.IOException;

import static ru.ac.checkpointmanager.utils.ErrorUtils.errorsList;

@RestController
@RequiredArgsConstructor
@RequestMapping("chpman/authentication")
@Tag(name = "Регистрация и аутентификация", description = "Точка входа в приложение")
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

    @Operation(summary = "Регистрация нового пользователя", responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: Данные валидны, письмо для подтверждения отправлено",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserAuthDTO.class))}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            BAD_REQUEST: Ошибки валидации:
                            Имя: только латиница/кириллица, каждое новое слово начинается с заглавной;
                            Дата рождения: не больше текущей даты;
                            Телефон: 11-20 символов;
                            Email: валидация по RFC 5322;
                            Пароль: без пробелов, 6-20 символов"""
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: Email/телефон уже заняты"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR: Ошибка отправки email или другая серверная ошибка"
            )
    })
    @PostMapping("/registration")
    public ResponseEntity<?> register(@RequestBody @Valid UserAuthDTO user,
                                      BindingResult result
    ) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(errorsList(result), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(authenticationService.preRegister(user), HttpStatus.OK);
    }

    @Operation(summary = "Подтверждение регистрации по ссылке")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: Регистрация подтверждена"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: Ссылка подтверждения недействительна или истек срок действия"
            )
    })
    @GetMapping("/confirm")
    public ResponseEntity<String> confirmRegistration(
            @Parameter(description = "Токен из письма для подтверждения")
            @RequestParam("token") String token
    ) {
        authenticationService.confirmRegistration(token);
        return ResponseEntity.ok("Регистрация подтверждена, войдите, используя указанные при регистрации email и пароль");
    }

    @PostMapping("/login")
    @Operation(
            summary = "Аутентификация пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK: Аутентификация прошла успешно",
                            content = @Content(
                                    schema = @Schema(implementation = AuthenticationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "BAD_REQUEST: неверный логин или пароль"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса"
                    )
            }
    )
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request,
                                          BindingResult result
    ) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Обновление токена",
            description = "Эндпоинт для обновления токена доступа, используя токен обновления",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK: Токен успешно обновлен",
                            content = @Content(
                                    schema = @Schema(implementation = AuthenticationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "UNAUTHORIZED: Недействительный токен обновления или проблемы с аутентификацией пользователя"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса на обновление токена"
                    )
            }
    )
    public void refreshToken(HttpServletRequest request, HttpServletResponse response
    ) throws IOException {
        authenticationService.refreshToken(request, response);
    }
}
