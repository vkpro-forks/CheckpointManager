package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.user.AuthRequestDTO;
import ru.ac.checkpointmanager.dto.user.AuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.LoginResponseDTO;
import ru.ac.checkpointmanager.dto.user.PreAuthResponseDTO;
import ru.ac.checkpointmanager.dto.user.RefreshTokenDTO;
import ru.ac.checkpointmanager.dto.user.RegistrationConfirmationDTO;
import ru.ac.checkpointmanager.dto.user.RegistrationDTO;
import ru.ac.checkpointmanager.service.auth.AuthenticationService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("api/v1/authentication")
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
                    responseCode = "201",
                    description = "CREATED: Данные валидны, письмо для подтверждения отправлено",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = RegistrationDTO.class))}
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            BAD_REQUEST: Ошибки валидации:
                            Имя: только латиница/кириллица, каждое новое слово начинается с заглавной;
                            Email: валидация по RFC 5322;
                            Пароль: без пробелов, 6-20 символов"""
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: Email уже используется"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR: Ошибка отправки email или другая серверная ошибка"
            )
    })
    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationConfirmationDTO register(@RequestBody @Valid RegistrationDTO user) {
        return authenticationService.preRegister(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Аутентификация пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK: Аутентификация прошла успешно",
                            content = @Content(
                                    schema = @Schema(implementation = LoginResponseDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "UNAUTHORIZED: неверный логин или пароль/пользователь заблокирован"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса"
                    )
            }
    )
    public ResponseEntity<?> authenticate(@RequestBody AuthRequestDTO request,
                                          BindingResult result
    ) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Обновление токена",
            description = "Эндпоинт для обновления токена доступа, используя токен обновления",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK: Токен успешно обновлен",
                            content = @Content(
                                    schema = @Schema(implementation = AuthResponseDTO.class),
                                    mediaType = MediaType.APPLICATION_JSON_VALUE
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "FORBIDDEN: Невалидный токен обновления"
                    )
            }
    )
    public AuthResponseDTO refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        return authenticationService.refreshToken(refreshTokenDTO);
    }

    @Operation(summary = "Проверка регистрации при входе в сервис")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: Ответ получен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PreAuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST: Параметры не были переданы"
            )
    })
    @GetMapping("/is-authenticated")
    public ResponseEntity<?> isUserAuthenticated(@RequestParam String email) {
        PreAuthResponseDTO response = authenticationService.isUserAuthenticated(email);
        return ResponseEntity.ok(response);
    }
}