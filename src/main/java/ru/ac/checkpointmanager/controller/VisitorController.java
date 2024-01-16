package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.service.visitor.VisitorService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/visitor")
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Visitor (Посетитель)", description = "Работа со списком посетителей.")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "Не авторизован."),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса.")})
public class VisitorController {

    private final VisitorService visitorService;

    @Operation(summary = "Добавить нового посетителя.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Посетитель успешно добавлен.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VisitorDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VisitorDTO addVisitor(@Valid @RequestBody VisitorDTO visitorDTO) {
        return visitorService.addVisitor(visitorDTO);
    }

    @Operation(summary = "Получение посетителя по id.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель получен.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VisitorDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Посетитель не существует.")
    })
    @GetMapping("/{id}")
    public VisitorDTO getVisitor(@PathVariable UUID id) {
        return visitorService.getVisitor(id);
    }

    @Operation(summary = "Обновить информацию о посетителе по ID",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Посетитель успешно обновлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VisitorDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей."),
            @ApiResponse(responseCode = "404", description = "Посетителя с таким ID не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PutMapping("/{id}")
    public VisitorDTO updateVisitor(@PathVariable UUID id, @RequestBody @Valid VisitorDTO visitorDTO) {
        return visitorService.updateVisitor(id, visitorDTO);
    }

    @Operation(summary = "Удалить посетителя.",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Посетитель успешно удален"),
            @ApiResponse(responseCode = "404", description = "Посетитель не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVisitor(@PathVariable UUID id) {
        visitorService.deleteVisitor(id);
    }

    @Operation(summary = "Найти посетителя по номеру телефона (части номера).",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно найден.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VisitorDTO.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/phone")
    public List<VisitorDTO> searchByPhonePart(@RequestParam @NotBlank String phone) {
        return visitorService.findByPhonePart(phone);
    }


    @Operation(summary = "Найти посетителя по имени (части имени).",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно найден.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VisitorDTO.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/name")
    public List<VisitorDTO> searchByNamePart(@RequestParam @NotBlank String name) {
        return visitorService.findByNamePart(name);
    }

    @Operation(summary = "Найти посетителя по Id пропуска.",
            description = "Доступ: ADMIN, MANAGER, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно найден.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VisitorDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Посетитель с id пропуска, не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/pass/{passId}")
    public VisitorDTO searchByPassId(@PathVariable UUID passId) {
        return visitorService.findByPassId(passId);
    }

    @Operation(summary = "Найти посетителя по Id пользователя",
            description = "Доступ: ADMIN, MANAGER, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно найден",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VisitorDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Посетитель с id user, не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/user/{userId}")
    public List<VisitorDTO> searchByUserId(@PathVariable UUID userId) {
        return visitorService.findByUserId(userId);
    }

}
