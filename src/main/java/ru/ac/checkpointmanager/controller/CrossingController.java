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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.mapper.CrossingMapper;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.service.crossing.CrossingService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("api/v1/crossing")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Crossing (Пересечение)", description = "Управление пересечениями")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "Нужно авторизоваться"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
//я предполагаю, что этот эндпоинт будет вызываться когда будет открываться шлагбаум(например) и тем самым фиксироваться пересечение
public class CrossingController {

    private final CrossingService crossingService;
    private final CrossingMapper mapper;

    @Operation(summary = "Создание пересечения, имитирует проезд или проход объекта через КПП",
            description = "Доступ: ADMIN, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пересечение успешно добавлено.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Crossing.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей.")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SECURITY')")
    @PostMapping("/mark")
    public ResponseEntity<?> markCrossing(@Valid @RequestBody CrossingDTO crossingDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Failed to mark crossing due to validation errors");
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Crossing crossing = crossingService.markCrossing(mapper.toCrossing(crossingDTO));
        log.info("Crossing marked: {}", crossing);
        return new ResponseEntity<>(mapper.toCrossingDTO(crossing), HttpStatus.OK);
    }

    @Operation(summary = "Получить пересечение по Id",
            description = "Доступ: ADMIN, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пересечение успешно получено.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Crossing.class))}),
            @ApiResponse(responseCode = "404", description = "Пересечения с таким Id не найдено.")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SECURITY')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCrossing(@PathVariable UUID id) {
        Crossing existCrossing = crossingService.getCrossing(id);
        return new ResponseEntity<>(mapper.toCrossingDTO(existCrossing), HttpStatus.OK);
    }
}
