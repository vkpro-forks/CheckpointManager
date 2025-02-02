package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.service.phone.PhoneService;

import java.util.Collection;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.SwaggerConstants.INTERNAL_SERVER_ERROR_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UNAUTHORIZED_MSG;


@RestController
@RequestMapping("api/v1/phones")
@Validated
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Телефоны пользователей", description = "API для управления телефонными номерами пользователей")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401",
                description = UNAUTHORIZED_MSG),
        @ApiResponse(responseCode = "500",
                description = INTERNAL_SERVER_ERROR_MSG)})
public class PhoneController {

    private final PhoneService phoneService;

    @Operation(summary = "Создание нового номера телефона",
            description = "Доступ: USER, SECURITY - со своим id; ADMIN, MANAGER - с любым.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "OK: номер сохранен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhoneDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST: Ошибка валидации: 11-20 символов, только цифры, пробелы и символы '(', ')', '-', '+'"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: пользователь для присвоения номера не найден"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: номер уже существует"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER') or @userAuthFacade.isIdMatch(#phoneDTO.userId)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PhoneDTO createPhoneNumber(@Valid @RequestBody PhoneDTO phoneDTO) {
        return phoneService.createPhoneNumber(phoneDTO);
    }

    @Operation(summary = "Поиск номера по id",
            description = "Доступ: USER - со своим id; ADMIN, MANAGER, SECURITY - с любым.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращается найденный телефон",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhoneDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: номера с таким id не найдено"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY') or @phoneAuthFacade.isIdMatch(#phoneId)")
    @GetMapping("/{phoneId}")
    public PhoneDTO findById(@Parameter(description = "Уникальный идентификатор телефона")
                             @PathVariable UUID phoneId) {
        return phoneService.findById(phoneId);
    }

    @Operation(summary = "Получить список всех номеров",
            description = "Доступ: ADMIN, MANAGER, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращает список номеров",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PhoneDTO.class))
                    )
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping
    public ResponseEntity<Collection<PhoneDTO>> getAll() {
        Collection<PhoneDTO> phones = phoneService.getAll();
        return ResponseEntity.ok(phones);
    }

    @Operation(summary = "Изменение параметров телефона",
            description = "Доступ: USER, SECURITY - со своим id; ADMIN, MANAGER - с любым.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: параметры телефона успешно изменены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhoneDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "CONFLICT: номер уже существует"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: телефон не найден"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER') or @phoneAuthFacade.isIdMatch(#phoneDTO.id)")
    @PutMapping
    public PhoneDTO updateNumber(@Valid @RequestBody PhoneDTO phoneDTO) {
        return phoneService.updatePhoneNumber(phoneDTO);
    }

    @Operation(summary = "Удалить телефон по id",
            description = "Доступ: USER, SECURITY - со своим id; ADMIN, MANAGER - с любым.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "NO CONTENT: телефон удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: телефон не найден"
            )
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER') or @phoneAuthFacade.isIdMatch(#phoneId)")
    @DeleteMapping("/{phoneId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNumber(@Parameter(description = "Уникальный идентификатор телефона")
                             @PathVariable UUID phoneId) {
        phoneService.deletePhoneNumber(phoneId);
    }
}
