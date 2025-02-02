package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.annotation.PagingParam;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.dto.user.UserFilterParams;
import ru.ac.checkpointmanager.dto.user.EmailConfirmationDTO;
import ru.ac.checkpointmanager.dto.user.NewEmailDTO;
import ru.ac.checkpointmanager.dto.user.NewPasswordDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.dto.user.UserUpdateDTO;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.service.user.UserService;
import ru.ac.checkpointmanager.specification.model.User_;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.SwaggerConstants.INTERNAL_SERVER_ERROR_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UNAUTHORIZED_MSG;

@RestController
@RequestMapping("api/v1/users")
@Validated
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Пользовательский интерфейс", description = "Комплекс операций по управлению жизненным циклом " +
        "пользовательских учетных записей, включая создание, модификацию, просмотр и удаление аккаунтов")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401",
                description = UNAUTHORIZED_MSG),
        @ApiResponse(responseCode = "500",
                description = INTERNAL_SERVER_ERROR_MSG)})
public class UserController {

    private final UserService userService;

    @Operation(summary = "Получить список всех пользователей",
            description = "Доступ: ADMIN",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20"),
                    @Parameter(in = ParameterIn.QUERY, name = User_.TERRITORIES),
                    @Parameter(in = ParameterIn.QUERY, name = User_.ROLE),
                    @Parameter(in = ParameterIn.QUERY, name = User_.IS_BLOCKED),
                    @Parameter(in = ParameterIn.QUERY, name = "part")
            })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращает страницу пользователей, содержащую объекты UserResponseDTO",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            )
    })
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping()
    public Page<UserResponseDTO> getAll(@Schema(hidden = true) @Valid @PagingParam PagingParams pagingParams,
                                        @Schema(hidden = true) UserFilterParams filterParams,
                                        @Schema(hidden = true) @RequestParam(value = "part", required = false) String part) {
        return userService.getAll(pagingParams, filterParams, part);
    }

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
    @GetMapping("/{userId}")
    public UserResponseDTO findUserById(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                        @PathVariable UUID userId) {
        return userService.findById(userId);
    }

    @Operation(summary = "Поиск территорий по id пользователя",
            description = "Доступ: USER, MANAGER, SECURITY - со своим id; ADMIN - с любым."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращется список привязанных территорий",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TerritoryDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN') or @userAuthFacade.isIdMatch(#userId)")
    @GetMapping("/{userId}/territories")
    public List<TerritoryDTO> getTerritoriesByUser(
            @Parameter(description = "Уникальный идентификатор пользователя", required = true)
            @PathVariable UUID userId) {
        return userService.findTerritoriesByUserId(userId);
    }

    @Operation(summary = "Поиск общих с конкретным пользователем территорий",
            description = "Доступ: ADMIN, MANAGER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращется список привязанных территорий",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TerritoryDTO.class)))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/{userId}/common_territories")
    public List<TerritoryDTO> getCommonTerritoriesByUser(
            @Parameter(description = "Уникальный идентификатор пользователя", required = true)
            @PathVariable UUID userId) {
        return userService.findCommonTerritoriesByUserId(userId);
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
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/name")
    public Collection<UserResponseDTO> findUserByName(
            @Parameter(description = "имя/часть имени", required = true)
            @RequestParam String name) {
        return userService.findByName(name);
    }

    @Operation(summary = "Получить список пользователей, связанных с территориями менеджера",
            description = "Доступ: MANAGER",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20"),
                    @Parameter(in = ParameterIn.QUERY, name = User_.TERRITORIES),
                    @Parameter(in = ParameterIn.QUERY, name = User_.ROLE),
                    @Parameter(in = ParameterIn.QUERY, name = User_.IS_BLOCKED),
                    @Parameter(in = ParameterIn.QUERY, name = "part")
            })
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращает страницу пользователей, содержащую объекты UserResponseDTO",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            )
    })
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    @GetMapping("/associated/")
    public Page<UserResponseDTO> getTerritoriesAssociatedUsers(@Schema(hidden = true) @Valid @PagingParam PagingParams pagingParams,
                                                               @Schema(hidden = true) UserFilterParams filterParams,
                                                               @Schema(hidden = true)
                                                               @RequestParam(value = "part", required = false) String part) {
        return userService.getTerritoriesAssociatedUsers(pagingParams, filterParams, part);
    }

    @Operation(summary = "Поиск пользователя по почте",
            description = "Доступ: ADMIN, MANAGER."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращает пользователя с указанной почтой",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN: роль пользователя не предоставляет доступ к данному api"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: совпадения по почте не найдены"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/email")
    public UserResponseDTO getByEmail(@RequestParam String email) {
        return userService.findByEmail(email);
    }

    @Operation(summary = "Получения списка номеров телефона, привязанных к пользователю",
            description = "Доступ: USER - со своим id; ADMIN, MANAGER, SECURITY - с любым"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращает список номеров, привязанных к пользователю",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = String.class)))
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY') or @userAuthFacade.isIdMatch(#userId)")
    @GetMapping("/{userId}/phones")
    public Collection<String> findUsersPhoneNumbers(
            @Parameter(description = "Уникальный идентификатор пользователя", required = true)
            @PathVariable UUID userId) {
        return userService.findUsersPhones(userId);
    }

    @Operation(summary = "Изменение данных пользователя",
            description = "Доступ: USER, SECURITY - со своим id; ADMIN, MANAGER - с любым."
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER') or @userAuthFacade.isIdMatch(#userUpdateDTO.id)")
    @PutMapping
    public UserResponseDTO updateUser(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        return userService.updateUser(userUpdateDTO);
    }

    @Operation(summary = "Изменение пароля пользователя",
            description = "Доступ: USER, ADMIN, MANAGER, SECURITY"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "NO_CONTENT: пароль пользователя успешно изменен"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST: новый пароль не совпадает с паролем подтверждения"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: Передан неверный текущий пароль пользователя"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody @Valid NewPasswordDTO request) {
        userService.changePassword(request);
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
                    description = "CONFLICT: указанная почта уже используется"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PatchMapping("/email")
    public EmailConfirmationDTO changeEmail(@RequestBody @Valid NewEmailDTO request) {
        return userService.changeEmail(request);
    }

    @Operation(summary = "Изменение роли пользователя",
            description = "Доступ: ADMIN, MANAGER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "NO_CONTENT: роль пользователя успешно изменена"
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
                    responseCode = "404",
                    description = "NOT_FOUND: пользователь не найден"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: назначенная роль совпадает с текущей ролью"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/role/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeRole(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                           @PathVariable UUID id,
                           @Parameter(description = "Новая роль пользователя", required = true)
                           @RequestParam Role role) {
        userService.changeRole(id, role);
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
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/{userId}")
    public UserResponseDTO updateBlockStatus(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                                             @PathVariable UUID userId,
                                             @Parameter(description = "Статус блокировки: true для блокировки пользователя, false для разблокировки", required = true)
                                             @RequestParam Boolean isBlocked) {
        return userService.updateBlockStatus(userId, isBlocked);
    }

    @Operation(summary = "Заблокировать пользователя по id",
            description = "Доступ: ADMIN, MANAGER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "NO_CONTENT: пользователь заблокирован"
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/block/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void blockById(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                          @PathVariable UUID userId) {
        userService.blockById(userId);
    }

    @Operation(summary = "Разблокировать пользователя по id",
            description = "Доступ: ADMIN, MANAGER"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "NO_CONTENT: пользователь разблокирован"
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/unblock/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblockById(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                            @PathVariable UUID userId) {
        userService.unblockById(userId);
    }

    @Operation(summary = "Удалить пользователя по id",
            description = "Доступ: USER, SECURITY, MANAGER - со своим id; ADMIN - с любым."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "NO CONTENT: пользователь удален"
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
    @PreAuthorize("hasAnyRole('ROLE_ADMIN') or @userAuthFacade.isIdMatch(#userId)")
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@Parameter(description = "Уникальный идентификатор пользователя", required = true)
                           @PathVariable UUID userId) {
        userService.deleteUser(userId);
    }
}
