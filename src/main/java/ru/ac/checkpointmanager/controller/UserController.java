package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.ChangeEmailRequest;
import ru.ac.checkpointmanager.dto.ChangePasswordRequest;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserPutDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("chpman/user")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Пользовательский интерфейс", description = "Комплекс операций по управлению жизненным циклом " +
                                                        "пользовательских учетных записей, включая создание, модификацию, просмотр и удаление аккаунтов")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401",
                description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500",
                description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
public class UserController {

    private final UserService userService;

    @Operation(summary = "Поиск пользователя по id",
            description = "Доступ: USER, ADMIN, MANAGER, SECURITY."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращается найденный пользователь",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDTO.class),
                            examples = {
                                    @ExampleObject(value = "{\n\"id\": \"123e4567-e89b-12d3-a456-426614174001\",\n\"name\": \"John Doe\",\n\"email\": \"john.doe@example.com\"\n}")
                            })
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: пользователя с таким id не найдено"
            )
    })
    @PreAuthorize(value = "hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("{id}")
    public ResponseEntity<UserResponseDTO> findUserById(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                                        @PathVariable UUID id
    ) {
        Optional<UserResponseDTO> user = Optional.ofNullable(userService.findById(id));
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Поиск территории по id пользователя",
            description = "Доступ: USER, ADMIN, MANAGER, SECURITY."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращется список привязанных территорий",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TerritoryDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: территории у переданного пользователя не найдены"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/{userId}/territories")
    public ResponseEntity<List<TerritoryDTO>> getTerritoriesByUser(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                                                   @PathVariable UUID userId
    ) {
        List<TerritoryDTO> territories = userService.findTerritoriesByUserId(userId);
        return territories.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(territories);
    }

    @Operation(summary = "Поиск пользователя по имени",
            description = "Доступ: USER, ADMIN, MANAGER, SECURITY."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращает список пользователей, имена которых содержат указанный в параметрах запроса элемент",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: совпадения по имени не найдены"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/name")
    public ResponseEntity<Collection<UserResponseDTO>> findUserByName(@Parameter(description = "имя/часть имени", required = true)
                                                                      @RequestParam String name
    ) {
        Collection<UserResponseDTO> foundUsers = userService.findByName(name);
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @Operation(summary = "Получить список всех пользователей",
            description = "Доступ: ADMIN, MANAGER, SECURITY."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращает список пользователей",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: в базе нет пользователей"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping()
    public ResponseEntity<Collection<UserResponseDTO>> getAll() {
        Collection<UserResponseDTO> foundUsers = userService.getAll();
        return foundUsers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(foundUsers);
    }

    @Operation(summary = "Получения списка номеров телефона, привязанных к пользователю",
            description = "Доступ: USER, ADMIN, MANAGER, SECURITY."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращает список номеров, привязанных к пользователю",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = String.class)))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: пользователь не найден"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/numbers/{id}")
    public ResponseEntity<Collection<String>> findUsersPhoneNumbers(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                                                    @PathVariable UUID id
    ) {
        Collection<String> numbers = userService.findUsersPhoneNumbers(id);
        return numbers.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(numbers);
    }

    @Operation(summary = "Изменение данных пользователя",
            description = "Доступ: USER, ADMIN, MANAGER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: данные пользователя успешно изменены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            BAD_REQUEST: Ошибки валидации:
                            Имя: только латиница/кириллица, каждое новое слово начинается с заглавной;
                            Дата рождения: не больше текущей даты;
                            Телефон: 11-20 символов"""
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: пользователь не найден"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserPutDTO userPutDTO, BindingResult result
    ) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }
        UserResponseDTO changedUser = userService.updateUser(userPutDTO);
        return new ResponseEntity<>(changedUser, HttpStatus.OK);
    }

    @Operation(summary = "Изменение пароля пользователя",
            description = "Доступ: USER, ADMIN, MANAGER, SECURITY"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: пароль пользователя успешно изменен"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = """
                            CONFLICT:
                            1. Передан неверный текущий пароль пользователя;
                            2. Новый пароль не совпадает с паролем подтверждения."""
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody @Valid ChangePasswordRequest request,
                                            BindingResult result
    ) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Изменение почты пользователя",
            description = "Доступ: USER, ADMIN, MANAGER, SECURITY"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: письмо для подтверждения отправлено",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = String.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: Передана неверная текущая почта пользователя"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PatchMapping("/email")
    public ResponseEntity<?> changeEmail(@RequestBody @Valid ChangeEmailRequest request,
                                         BindingResult result
    ) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(userService.changeEmail(request));
    }

    @Operation(summary = "Изменение роли пользователя",
            description = "Доступ: ADMIN, MANAGER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: роль пользователя успешно изменена"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = """
                            FORBIDDEN:
                            1. Роль пользователя не предоставляет доступ к данному api;
                            2. Пользователь без прав администратора пытается назначить кому-либо роль ADMIN;
                            3. Пользователь без прав администратора пытается изменить роль с ADMIN на другую."""
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: назначенная роль совпадает с текущей ролью"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/role/{id}")
    public ResponseEntity<?> changeRole(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                        @PathVariable UUID id,
                                        @Parameter(description = "Новая роль пользователя", required = true)
                                        @RequestParam Role role
    ) {
        userService.changeRole(id, role);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Изменение статуса блокировки пользователя по id",
            description = "Доступ: ADMIN, MANAGER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: статус блокировки пользователя успешно изменен"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: пользователь не найден"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: назначенный статус блокировки совпадает с текущим"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("{id}")
    public ResponseEntity<?> updateBlockStatus(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                               @PathVariable UUID id,
                                               @Parameter(description = "Статус блокировки: true для блокировки пользователя, false для разблокировки", required = true)
                                               @RequestParam Boolean isBlocked
    ) {
        UserResponseDTO changedUser = userService.updateBlockStatus(id, isBlocked);
        return ResponseEntity.ok(changedUser);
    }

    @Operation(summary = "Заблокировать пользователя по id",
            description = "Доступ: ADMIN, MANAGER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: пользователь заблокирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: пользователь не найден"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: назначенный статус блокировки совпадает с текущим"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/block/{id}")
    public ResponseEntity<?> blockById(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                       @PathVariable UUID id
    ) {
        userService.blockById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Разблокировать пользователя по id",
            description = "Доступ: ADMIN, MANAGER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: пользователь разблокирован"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: пользователь не найден"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: назначенный статус блокировки совпадает с текущим"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/unblock/{id}")
    public ResponseEntity<?> unblockById(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                         @PathVariable UUID id
    ) {
        userService.unblockById(id);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Удалить пользователя по id",
            description = "Доступ: ADMIN, MANAGER, USER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: пользователь удален"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: пользователь не найден"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                           @PathVariable UUID id
    ) {
        UserResponseDTO foundUser = userService.findById(id);
        if (foundUser == null) {
            return ResponseEntity.noContent().build();
        }
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }
}