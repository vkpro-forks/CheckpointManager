package ru.ac.checkpointmanager.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.dto.CrossingRequestDTO;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.service.crossing.CrossingService;

import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.SwaggerConstants.BAD_REQUEST_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.INTERNAL_SERVER_ERROR_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UNAUTHORIZED_MSG;


@Slf4j
@RestController
@RequestMapping("api/v1/crossing")
@Validated
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Crossing (Пересечение)", description = "Работа с пересечениями машин и посетителей через КПП")
@ApiResponses(value = {
        @ApiResponse(responseCode = "401", description = UNAUTHORIZED_MSG,
                content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
        @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR_MSG,
                content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
})
public class CrossingController {

    private final CrossingService crossingService;

    @Operation(summary = "Создание события: въезд/вход на территорию",
            description = "Доступ: ADMIN, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пересечение успешно добавлено.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CrossingDTO.class))}),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SECURITY')")
    @PostMapping("/in")
    public CrossingDTO addCrossingIn(@Valid @RequestBody CrossingRequestDTO crossingDTO) {
        return crossingService.addCrossing(crossingDTO, Direction.IN);
    }

    @Operation(summary = "Создание события: выезд/выход с территории",
            description = "Доступ: ADMIN, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пересечение успешно добавлено.",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CrossingDTO.class))}),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SECURITY')")
    @PostMapping("/out")
    public CrossingDTO addCrossingOut(@Valid @RequestBody CrossingRequestDTO crossingDTO) {
        return crossingService.addCrossing(crossingDTO, Direction.OUT);

    }

    /**
     * Will be replaced for in/out endpoints
     *
     * @deprecated
     */
    @Operation(summary = "Создание события: въезд, выезд",
            description = "Доступ: ADMIN, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пересечение успешно добавлено",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CrossingDTO.class))}),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @Deprecated(forRemoval = true, since = "17.12.2023")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SECURITY')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrossingDTO addCrossing(@Valid @RequestBody CrossingRequestDTO crossingDTO) {
        return crossingService.addCrossing(crossingDTO, null);
    }

    @Operation(summary = "Получить пересечение по Id",
            description = "Доступ: ADMIN, SECURITY.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пересечение успешно получено",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Crossing.class))}),
            @ApiResponse(responseCode = "400", description = BAD_REQUEST_MESSAGE,
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_SECURITY')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCrossing(@PathVariable UUID id) {
        CrossingDTO crossing = crossingService.getCrossing(id);
        return new ResponseEntity<>(crossing, HttpStatus.OK);
    }

    @Operation(summary = "Получить список пересечений по id пропуска",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Найдены пересечения по пропуску",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CrossingDTO.class)))}),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")
    })
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @GetMapping("/pass/{id}")
    public ResponseEntity<List<CrossingDTO>> getByPassId(@PathVariable UUID id) {
        List<CrossingDTO> foundCrossings = crossingService.getByPassId(id);
        return ResponseEntity.ok(foundCrossings);
    }
}
