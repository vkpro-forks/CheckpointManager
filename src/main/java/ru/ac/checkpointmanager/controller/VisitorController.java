package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.mapper.VisitorMapper;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.service.visitor.VisitorService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/visitor")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Visitor (Посетитель)", description = "Работа со списком посетителей.")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "Не авторизован."),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса.")})
public class VisitorController {

    private final VisitorService visitorService;
    private final VisitorMapper mapper;

    @Operation(summary = "Добавить нового посетителя.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Посетитель успешно добавлен.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PostMapping
    public ResponseEntity<?> addVisitor(@Valid @RequestBody VisitorDTO visitorDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Failed to add visitor due to validation errors");
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Visitor newVisitor = visitorService.addVisitor(mapper.toVisitor(visitorDTO));
        log.info("New visitor added: {}", newVisitor);
        return new ResponseEntity<>(mapper.toVisitorDTO(newVisitor), HttpStatus.CREATED);
    }

    @Operation(summary = "Получение посетителя по id.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель получен.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "404", description = "Посетитель не существует.")
    })
    @GetMapping("/{id}")
    public VisitorDTO getVisitor(@PathVariable UUID id) {
        Visitor existVisitor = visitorService.getVisitor(id);
        log.debug("Retrieved visitor with ID {}", id);
        return mapper.toVisitorDTO(existVisitor);
    }


    @Operation(summary = "Обновить информацию о посетителе по ID",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Посетитель успешно обновлен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей."),
            @ApiResponse(responseCode = "404", description = "Посетителя с таким ID не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVisitor(@PathVariable UUID id, @RequestBody VisitorDTO visitorDTO,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Failed to update visitor due to validation errors");
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Visitor visitor = mapper.toVisitor(visitorDTO);
        Visitor updatedVisitor = visitorService.updateVisitor(id, visitor);
        log.info("Visitor updated with ID {}", id);
        return new ResponseEntity<>(mapper.toVisitorDTO(updatedVisitor), HttpStatus.OK);
    }


    @Operation(summary = "Удалить посетителя.",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно удален",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "404", description = "Посетитель не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVisitor(@PathVariable UUID id) {
        visitorService.deleteVisitor(id);
        log.info("Visitor with ID {} deleted", id);
        return new ResponseEntity<>("Visitor deleted " + id, HttpStatus.OK);
    }

    @Operation(summary = "Найти посетителя по номеру телефона.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно найден.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/phone")
    public List<VisitorDTO> searchByPhone(@RequestParam String phone) { //TODO validate
        List<Visitor> visitors = visitorService.findByPhonePart(phone);
        log.debug("Visitors found with phone part: {}", phone);
        return mapper.toVisitorDTOS(visitors);
    }


    @Operation(summary = "Найти посетителя по имени.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно найден.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/name")
    public List<VisitorDTO> searchByName(@RequestParam String name) {
        List<Visitor> visitors = visitorService.findByNamePart(name);
        log.debug("Visitors found with name part: {}", name);
        return mapper.toVisitorDTOS(visitors);
    }

    @Operation(summary = "Найти посетителя по Id пропуска.",
            description = "Доступ: ADMIN, MANAGER, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно найден.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "404", description = "Посетитель с id пропуска, не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/pass")
    public ResponseEntity<?> searchByPassId(@RequestParam UUID uuid) {
        Visitor existVisitor = visitorService.findByPassId(uuid).orElse(null);
        log.debug("Visitor found for pass ID: {}", uuid);
        return new ResponseEntity<>(mapper.toVisitorDTO(existVisitor), HttpStatus.OK);
    }


    @Operation(summary = "Найти посетителя по Id user",
            description = "Доступ: ADMIN, MANAGER, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "404", description = "Посетитель с id user, не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VisitorDTO>> searchByUserId(@PathVariable UUID userId) {
        List<Visitor> visitors = visitorService.findByUserId(userId);
        log.debug("Visitors found for user ID: {}", userId);
        return new ResponseEntity<>(mapper.toVisitorDTOS(visitors), HttpStatus.OK);
    }

}
