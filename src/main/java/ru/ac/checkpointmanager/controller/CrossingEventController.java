package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.annotation.PagingParam;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.projection.PassInOutView;
import ru.ac.checkpointmanager.service.event.PassInOutViewService;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/event")
@RequiredArgsConstructor
@Validated
@Tag(name = "Event (события въезд-выезд (пары))", description = "Получение событий по пропуску (пары въезд-выезд")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
@SecurityRequirement(name = "bearerAuth")
public class CrossingEventController {

    private final PassInOutViewService passInOutViewService;

    @Operation(summary = "Получить список событий по пропускам пользователя",
            description = "Доступ: ADMIN, USER.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "События найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassInOutView.class)))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}")
    public Page<PassInOutView> getEventsByUserId(@PathVariable UUID userId,
                                                 @Schema(hidden = true)
                                                           @Valid @PagingParam PagingParams pagingParams) {
        return passInOutViewService.findEventsByUser(userId, pagingParams);
    }

    @Operation(summary = "Получить список событий по конкретной территории",
            description = "Доступ: ADMIN, MANAGER, SECURITY.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "События найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassInOutView.class)))),
            @ApiResponse(responseCode = "404", description = "Территория не найдена")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/territory/{territoryId}")
    public Page<PassInOutView> getEventsByTerritoryId(@PathVariable UUID territoryId,
                                                      @Schema(hidden = true)
                                                                @Valid
                                                                @PagingParam PagingParams pagingParams) {
        return passInOutViewService.findEventsByTerritory(territoryId, pagingParams);
    }

}
