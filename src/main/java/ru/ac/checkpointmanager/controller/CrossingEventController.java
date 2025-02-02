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

import static ru.ac.checkpointmanager.utils.SwaggerConstants.ACCESS_ADMIN_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UNAUTHORIZED_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.INTERNAL_SERVER_ERROR_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.EVENTS_ARE_FOUND_MESSAGE;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
@Validated
@Tag(name = "Event (события)", description = "Получение сгруппированных по парам \"въезд-выезд\" пересечений")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = UNAUTHORIZED_MSG),
        @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR_MSG)})
@SecurityRequirement(name = "bearerAuth")
public class CrossingEventController {

    private final PassInOutViewService passInOutViewService;

    @Operation(summary = "Получить список событий по пропускам пользователя (для User)",
            description = "Доступ: ADMIN - события всех пользователей, USER - только свои",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = EVENTS_ARE_FOUND_MESSAGE,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassInOutView.class)))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_USER') and @userAuthFacade.isIdMatch(#userId))")
    @GetMapping("/users/{userId}")
    public Page<PassInOutView> getEventsByUserId(@PathVariable UUID userId,
                                                 @Schema(hidden = true)
                                                 @Valid @PagingParam PagingParams pagingParams) {
        return passInOutViewService.findEventsByUser(userId, pagingParams);
    }

    @Operation(summary = "Получить список событий по конкретной территории (для Security)",
            description = "Доступ: ADMIN - по всем территориям, SECURITY - только на закрепленной территории",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = EVENTS_ARE_FOUND_MESSAGE,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassInOutView.class)))),
            @ApiResponse(responseCode = "404", description = "Территория не найдена")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_SECURITY') and @territoryAuthFacade.isIdMatch(#territoryId))")
    @GetMapping("/territories/{territoryId}")
    public Page<PassInOutView> getEventsByTerritoryId(@PathVariable UUID territoryId,
                                                      @Schema(hidden = true)
                                                      @Valid
                                                      @PagingParam PagingParams pagingParams) {
        return passInOutViewService.findEventsByTerritory(territoryId, pagingParams);
    }

    @Operation(summary = "Получить список событий по всем привязанным к пользователю территориям (для Manager и Security)",
            description = "Доступ: ADMIN - по всем территориям, MANAGER, SECURITY - только на закрепленной территории",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = EVENTS_ARE_FOUND_MESSAGE,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassInOutView.class)))),
            @ApiResponse(responseCode = "404", description = "Пользователь или территории не найдены")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasAnyRole('ROLE_MANAGER', 'ROLE_SECURITY') and @userAuthFacade.isIdMatch(#userId))")
    @GetMapping("/users/{userId}/territories")
    public Page<PassInOutView> findEventsByUsersTerritories(@PathVariable UUID userId,
                                                            @Schema(hidden = true)
                                                            @Valid
                                                            @PagingParam PagingParams pagingParams) {
        return passInOutViewService.findEventsByUsersTerritories(userId, pagingParams);
    }

    @Operation(summary = "Получить весь список событий (для Admin)",
            description = ACCESS_ADMIN_MESSAGE,
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = EVENTS_ARE_FOUND_MESSAGE,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassInOutView.class))))})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping()
    public Page<PassInOutView> getAllEvents(@Schema(hidden = true) @Valid
                                            @PagingParam PagingParams pagingParams) {
        return passInOutViewService.findAll(pagingParams);
    }
}
