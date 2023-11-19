package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import ru.ac.checkpointmanager.model.Visitor;
import ru.ac.checkpointmanager.service.visitor.VisitorService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
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
    private final Mapper mapper;

    @Operation(summary = "Добавить новую посетитель")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Посетитель успешно добавлена"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
    })
    @PostMapping
    public ResponseEntity<?> addVisitor(@Valid @RequestBody VisitorDTO visitorDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Visitor newVisitor = visitorService.addVisitor(mapper.toVisitor(visitorDTO));
        return new ResponseEntity<>(mapper.toVisitorDTO(newVisitor), HttpStatus.CREATED);
    }

    @Operation(summary = "Получить посетитель по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель найден"),
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getVisitor(@PathVariable UUID id) {
        Visitor existVisitor = visitorService.getVisitor(id);
        return new ResponseEntity<>(mapper.toVisitorDTO(existVisitor), HttpStatus.OK);
    }

    @Operation(summary = "Обновить информацию о посетителе по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно обновлен"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVisitor(@PathVariable UUID id, @RequestBody VisitorDTO visitorDTO,
                                          BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Visitor visitor = mapper.toVisitor(visitorDTO);
        Visitor updateVisitor = visitorService.updateVisitor(id, visitor);
        return new ResponseEntity<>(mapper.toVisitorDTO(updateVisitor), HttpStatus.OK);
    }

    @Operation(summary = "Удалить посетитель по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель успешно удален"),
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVisitor(@PathVariable UUID id) {
        visitorService.deleteVisitor(id);
        return new ResponseEntity<>("Visitor deleted " + id, HttpStatus.OK);
    }

    @Operation(summary = "Найти посетителя по номеру телефона")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетители найдены"),
    })
    @GetMapping("/phone")
    public ResponseEntity<List<VisitorDTO>> searchByPhone(@RequestParam String phone) {
        List<Visitor> visitors = visitorService.findByPhonePart(phone);
        List<VisitorDTO> visitorDTOS = visitors.stream()
                .map(visitor -> mapper.toVisitorDTO(visitor)).toList();
        return new ResponseEntity<>(visitorDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Найти посетителя по имени")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетители найдены"),
    })
    @GetMapping("/name")
    public ResponseEntity<List<VisitorDTO>> searchByName(@RequestParam String name) {
        List<Visitor> visitors = visitorService.findByNamePart(name);
        List<VisitorDTO> visitorDTOS = visitors.stream()
                .map(visitor -> mapper.toVisitorDTO(visitor))
                .toList();
        return new ResponseEntity<>(visitorDTOS, HttpStatus.OK);
    }

    @Operation(summary = "Найти посетителя по ID пропуска")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Посетитель найден"),
            @ApiResponse(responseCode = "404", description = "Посетитель не найден"),
    })
    @GetMapping("/pass")
    public ResponseEntity<?> searchByPass(@RequestParam UUID uuid) {
        Visitor existVisitor = visitorService.findByPassId(uuid).orElse(null);
        if (uuid != null) {
            return new ResponseEntity<>(mapper.toVisitorDTO(existVisitor), HttpStatus.OK);
        }
        return new ResponseEntity<>("There is no such visitor in any pass! ", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Найти посетителей из пропусков пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Возвращен список посетителей"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VisitorDTO>> searchByUserId(@PathVariable UUID userId) {
        List<Visitor> visitors = visitorService.findByUserId(userId);
        List<VisitorDTO> visitorDTOS = mapper.toVisitorDTO(visitors);
        return new ResponseEntity<>(visitorDTOS, HttpStatus.OK);
    }
}
