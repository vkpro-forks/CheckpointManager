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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.service.phone.PhoneService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("chpman/phone")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
@Tag(name = "Телефоны пользователей", description = "API для управления телефонными номерами пользователей")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401",
                description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500",
                description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
public class PhoneController {

    private final PhoneService phoneService;

    @Operation(summary = "Создание нового номера телефона")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: номер сохранен",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhoneDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST: номер уже существует"
            )
    })
    @PostMapping
    public ResponseEntity<?> createPhoneNumber(@Valid @RequestBody PhoneDTO phoneDTO,
                                               BindingResult result
    ) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        PhoneDTO createdPhone = phoneService.createPhoneNumber(phoneDTO);
        return new ResponseEntity<>(createdPhone, HttpStatus.CREATED);
    }

    @Operation(summary = "Поиск номера по id")
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
    @GetMapping("{id}")
    public ResponseEntity<PhoneDTO> findById(@Parameter(description = "Уникальный идентификатор телефона")
                                             @PathVariable UUID id
    ) {
        Optional<PhoneDTO> foundPhone = Optional.ofNullable(phoneService.findById(id));
        return foundPhone.map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "Получить список всех номеров")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: возвращает список номеров",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PhoneDTO.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: в базе нет номеров"
            )
    })
    @GetMapping
    public ResponseEntity<Collection<PhoneDTO>> getAll() {
        Collection<PhoneDTO> phones = phoneService.getAll();
        return phones.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(phones);
    }

    @Operation(summary = "Изменение параметров телефона")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: параметры телефона успешно изменены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PhoneDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST: номер уже существует"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: телефон не найден"
            )
    })
    @PutMapping
    public ResponseEntity<?> updateNumber(@Valid @RequestBody PhoneDTO phoneDTO,
                                          BindingResult result
    ) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(result), HttpStatus.BAD_REQUEST);
        }

        PhoneDTO changedNumber = phoneService.updatePhoneNumber(phoneDTO);
        return ResponseEntity.ok(changedNumber);
    }

    @Operation(summary = "Удалить телефон по id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OK: телефон удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "NOT_FOUND: телефон не найден"
            )
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteNumber(@Parameter(description = "Уникальный идентификатор телефона")
                                             @PathVariable UUID id
    ) {
        PhoneDTO foundPhone = phoneService.findById(id);
        if (foundPhone == null) {
            return ResponseEntity.noContent().build();
        }
        phoneService.deletePhoneNumber(id);
        return ResponseEntity.ok().build();
    }
}
