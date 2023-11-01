package ru.ac.checkpointmanager.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.service.crossing.CrossingService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.UUID;


@RestController
@RequestMapping("chpman/crossing")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Crossing (Пересечение)", description = "Администрирование пересечений")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
//я предполагаю, что этот эндпоинт будет вызываться когда будет открываться шлагбаум(например) и тем самым фиксироваться пересечение
public class CrossingController {

    private final CrossingService crossingService;
    private final Mapper mapper;

    @Operation(summary = "Отметить пересечение")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пересечение успешно отмечено"),
            @ApiResponse(responseCode = "400", description = "Неверный запрос"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @PostMapping("/mark")
    public ResponseEntity<?> markCrossing(@Valid @RequestBody CrossingDTO crossingDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Crossing crossing = crossingService.markCrossing(mapper.toCrossing(crossingDTO));
        return new ResponseEntity<>(mapper.toCrossingDTO(crossing), HttpStatus.OK);
    }

    @Operation(summary = "Получить информацию о пересечении по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пересечение найдено"),
            @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getCrossing(@PathVariable UUID id) {
        Crossing existCrossing = crossingService.getCrossing(id);
        return new ResponseEntity<>(mapper.toCrossingDTO(existCrossing), HttpStatus.OK);
    }
}
