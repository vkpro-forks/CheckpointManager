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
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.VisitorDTO;
import ru.ac.checkpointmanager.mapper.VisitorMapper;
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.car.CarBrand;
import ru.ac.checkpointmanager.service.visitor.VisitorService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("chpman/visitor")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Visitor (Посетитель)", description = "Работа со списком гостей")
@ApiResponse(responseCode = "401", description = "Не авторизован")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class VisitorController {

    private final VisitorService visitorService;
    private final VisitorMapper mapper;

    @Operation(summary = "Добавить нового Визитера",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Визитер успешно добавлен",
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

    @Operation(summary = "Получение Визитера по id",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Визитер получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "404", description = "Визитера не существует.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getVisitor(@PathVariable UUID id) {
        Visitor existVisitor = visitorService.getVisitor(id);
        if (existVisitor == null) {
            log.warn("Failed to find visitor with ID {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.debug("Retrieved visitor with ID {}", id);
        return new ResponseEntity<>(mapper.toVisitorDTO(existVisitor), HttpStatus.OK);
    }

    @Operation(summary = "Обновить Визитера",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Визитер успешно обновлен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей."),
            @ApiResponse(responseCode = "404", description = "Визитера с таким ID не существует.")
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
        Visitor updateVisitor = visitorService.updateVisitor(id, visitor);
        if (updateVisitor == null) {
            log.warn("Failed to update visitor with ID {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Visitor updated with ID {}", id);
        return new ResponseEntity<>(mapper.toVisitorDTO(updateVisitor), HttpStatus.OK);
    }

    @Operation(summary = "Удалить Визитера",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Визитер успешно удален",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "404", description = "Визитера не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVisitor(@PathVariable UUID id) {
        if (!visitorService.existsById(id)) {
            log.warn("Failed to delete visitor: No visitor found with ID {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        visitorService.deleteVisitor(id);
        log.info("Visitor with ID {} deleted", id);
        return new ResponseEntity<>("Visitor deleted " + id, HttpStatus.OK);
    }

    @Operation(summary = "Найти Визитера по номеру телефона.",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Визитер успешно найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/phone")
    public List<VisitorDTO> searchByPhone(@RequestParam String phone) {
        List<Visitor> visitors = visitorService.findByPhonePart(phone);
        if (visitors.isEmpty()) {
            log.warn("No visitors found with phone part: {}", phone);
        } else {
            log.debug("Visitors found with phone part: {}", phone);
        }
        return mapper.toVisitorDTOS(visitors);
    }

    @Operation(summary = "Найти Визитера по имени",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Визитер успешно найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
    })
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/name")
    public List<VisitorDTO> searchByName(@RequestParam String name) {
        List<Visitor> visitors = visitorService.findByNamePart(name);
        if (visitors.isEmpty()) {
            log.warn("No visitors found with name part: {}", name);
        }
        log.debug("Visitors found with name part: {}", name);
        return mapper.toVisitorDTOS(visitors);
    }

    @Operation(summary = "Найти Визитера по Id пропуска",
            description = "Доступ: ADMIN, MANAGER, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Визитер успешно найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "404", description = "Визитера с id пропуска, не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/pass")
    public ResponseEntity<?> searchByPassId(@RequestParam UUID uuid) {
        Optional<Visitor> existVisitor = visitorService.findByPassId(uuid);
        if (existVisitor.isEmpty()) {
            log.warn("No visitor found for pass ID: {}", uuid);
            return new ResponseEntity<>("There is no such visitor in any pass!", HttpStatus.NOT_FOUND);
        }
        log.debug("Visitor found for pass ID: {}", uuid);
        return new ResponseEntity<>(mapper.toVisitorDTO(existVisitor.get()), HttpStatus.OK);
    }

    @Operation(summary = "Найти Визитера по Id user",
            description = "Доступ: ADMIN, MANAGER, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Визитер успешно найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = Visitor.class))}),
            @ApiResponse(responseCode = "404", description = "Визитера с id user, не существует.")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VisitorDTO>> searchByUserId(@PathVariable UUID userId) {
        List<Visitor> visitors = visitorService.findByUserId(userId);
        if (visitors.isEmpty()) {
            log.warn("No visitors found for user ID: {}", userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.debug("Visitors found for user ID: {}", userId);
        return new ResponseEntity<>(mapper.toVisitorDTOS(visitors), HttpStatus.OK);
    }
}
