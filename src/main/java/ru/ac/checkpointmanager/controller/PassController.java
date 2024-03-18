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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.annotation.PagingParam;
import ru.ac.checkpointmanager.dto.passes.FilterParams;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassResponseDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.specification.model.Pass_;

import java.util.UUID;

import static ru.ac.checkpointmanager.utils.SwaggerConstants.INTERNAL_SERVER_ERROR_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UNAUTHORIZED_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.ACCESS_ADMIN_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.PASSES_ARE_FOUND_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.PASS_NOT_FOUND_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.PASS_ACCESS_ALL_MESSAGE;

@RestController
@RequestMapping("api/v1/passes")
@RequiredArgsConstructor
@Validated
@Tag(name = "Pass (пропуска)", description = "Операции с пропусками для машин и людей")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = UNAUTHORIZED_MSG),
        @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR_MSG)})
@SecurityRequirement(name = "bearerAuth")
public class PassController {

    private final PassService service;

    /* CREATE */
    @Operation(summary = "Добавить новый пропуск",
            description = "Доступ: ADMIN - по всем территориям, MANAGER, SECURITY, USER - только для своих территорий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пропуск успешно добавлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @territoryAuthFacade.isIdMatch(#passCreateDTO.getTerritoryId)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PassResponseDTO addPass(@RequestBody @Valid PassCreateDTO passCreateDTO) {
        return service.addPass(passCreateDTO);
    }

    /* READ */
    @Operation(summary = "Получить список всех пропусков, с учетом фильтрации и совпадения" +
            " по фрагменту имени или номера авто",
            description = ACCESS_ADMIN_MESSAGE,
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page"),
                    @Parameter(in = ParameterIn.QUERY, name = "size"),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.DTYPE),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.TERRITORY),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.STATUS),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.FAVORITE),
                    @Parameter(in = ParameterIn.QUERY, name = "part")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = PASSES_ARE_FOUND_MESSAGE,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены")})
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public Page<PassResponseDTO> getPasses(@Schema(hidden = true) @Valid @PagingParam PagingParams pagingParams,
                                           @Schema(hidden = true) FilterParams filterParams,
                                           @Schema(hidden = true)
                                           @RequestParam(value = "part", required = false) String part) {
        return service.findPasses(pagingParams, filterParams, part);
    }

    @Operation(summary = "Найти пропуск по id",
            description = "Доступ: ADMIN - все пропуски, MANAGER, SECURITY - на закрепленной территории, " +
                    "USER - только свой")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск найден",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "404", description = PASS_NOT_FOUND_MESSAGE)})
    @PreAuthorize("hasRole('ROLE_ADMIN')" +
            "or (hasAnyRole('ROLE_MANAGER', 'ROLE_SECURITY') and @passAuthFacade.isTerritoryIdMatch(#passId)) " +
            "or (hasRole('ROLE_USER') and @passAuthFacade.isIdMatch(#passId))")
    @GetMapping("/{passId}")
    public PassResponseDTO getPass(@PathVariable UUID passId) {
        return service.findById(passId);
    }

    @Operation(summary = "Получить список пропусков конкретного пользователя, с учетом фильтрации и совпадения" +
            " по фрагменту имени или номера авто",
            description = "Доступ: ADMIN - пропуска всех пользователей, MANAGER, SECURITY, USER - только свои",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page"),
                    @Parameter(in = ParameterIn.QUERY, name = "size"),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.DTYPE),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.TERRITORY),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.STATUS),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.FAVORITE),
                    @Parameter(in = ParameterIn.QUERY, name = "part")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = PASSES_ARE_FOUND_MESSAGE,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userAuthFacade.isIdMatch(#userId)")
    @GetMapping("/users/{userId}")
    public Page<PassResponseDTO> getPassesByUserId(@PathVariable UUID userId,
                                                   @Schema(hidden = true) @Valid @PagingParam PagingParams pagingParams,
                                                   @Schema(hidden = true) FilterParams filterParams,
                                                   @RequestParam(value = "part", required = false) String part) {
        return service.findPassesByUser(userId, pagingParams, filterParams, part);
    }

    @Operation(summary = "Получить список пропусков на конкретную территорию, с учетом фильтрации и совпадения " +
            " по фрагменту имени или номера авто",
            description = "Доступ: ADMIN - поиск по всем пропускам, SECURITY - по своим территориям",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page"),
                    @Parameter(in = ParameterIn.QUERY, name = "size"),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.DTYPE),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.TERRITORY),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.STATUS),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.FAVORITE),
                    @Parameter(in = ParameterIn.QUERY, name = "part")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = PASSES_ARE_FOUND_MESSAGE,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Территория не найдена")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_SECURITY') and @territoryAuthFacade.isIdMatch(#territoryId))")
    @GetMapping("/territories/{territoryId}")
    public Page<PassResponseDTO> getPassesByTerritoryId(@PathVariable UUID territoryId,
                                                        @Schema(hidden = true)
                                                        @Valid @PagingParam PagingParams pagingParams,
                                                        @Schema(hidden = true) FilterParams filterParams,
                                                        @RequestParam(value = "part", required = false) String part) {
        return service.findPassesByTerritory(territoryId, pagingParams, filterParams, part);
    }

    @Operation(summary = "Получить список пропусков по всем привязанным к пользователю территориям, " +
            "с учетом фильтрации и совпадения по фрагменту имени или номера авто",
            description = "Доступ: ADMIN - поиск по всем пропускам, MANAGER, SECURITY - по своим территориям",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page"),
                    @Parameter(in = ParameterIn.QUERY, name = "size"),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.DTYPE),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.TERRITORY),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.STATUS),
                    @Parameter(in = ParameterIn.QUERY, name = Pass_.FAVORITE),
                    @Parameter(in = ParameterIn.QUERY, name = "part")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = PASSES_ARE_FOUND_MESSAGE,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Пользователь или территории не найдены")})
    @PreAuthorize("hasRole('ROLE_ADMIN') " +
            "or (hasAnyRole('ROLE_MANAGER', 'ROLE_SECURITY') and @userAuthFacade.isIdMatch(#userId))")
    @GetMapping("/users/{userId}/territories")
    public Page<PassResponseDTO> getPassesByUsersTerritories(@PathVariable UUID userId,
                                                             @Schema(hidden = true)
                                                             @Valid @PagingParam PagingParams pagingParams,
                                                             @Schema(hidden = true) FilterParams filterParams,
                                                             @RequestParam(value = "part", required = false) String part) {
        return service.findPassesByUsersTerritories(userId, pagingParams, filterParams, part);
    }

    /* UPDATE */
    @Operation(summary = "Изменить существующий пропуск",
            description = PASS_ACCESS_ALL_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно изменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @passAuthFacade.isIdMatch(#passUpdateDTO.getId)")
    @PutMapping
    public PassResponseDTO updatePass(@RequestBody @Valid PassUpdateDTO passUpdateDTO) {
        return service.updatePass(passUpdateDTO);
    }

    @Operation(summary = "Отменить активный пропуск",
            description = PASS_ACCESS_ALL_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск отменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Пропуск не является активным"),
            @ApiResponse(responseCode = "404", description = PASS_NOT_FOUND_MESSAGE)})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @passAuthFacade.isIdMatch(#passId)")
    @PatchMapping("/{passId}/cancel")
    public ResponseEntity<PassResponseDTO> cancelPass(@PathVariable UUID passId) {

        PassResponseDTO cancelledPass = service.cancelPass(passId);
        return ResponseEntity.ok(cancelledPass);
    }

    @Operation(summary = "Активировать отмененный пропуск",
            description = PASS_ACCESS_ALL_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск активирован",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Пропуск не является отмененным; время действия пропуска истекло"),
            @ApiResponse(responseCode = "404", description = PASS_NOT_FOUND_MESSAGE)})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @passAuthFacade.isIdMatch(#passId)")
    @PatchMapping("/{passId}/activate")
    public ResponseEntity<PassResponseDTO> activatePass(@PathVariable UUID passId) {

        PassResponseDTO activatedPass = service.activateCancelledPass(passId);
        return ResponseEntity.ok(activatedPass);
    }

    @Operation(summary = "Отметить выполненным пропуск со статусом \"нет выезда\"",
            description = "Доступ: ADMIN - поиск по всем пропускам, MANAGER, SECURITY - по своим территориям")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск отмечен выполненным",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Стутус отличен от Warning"),
            @ApiResponse(responseCode = "404", description = PASS_NOT_FOUND_MESSAGE)})
    @PreAuthorize("hasRole('ROLE_ADMIN') " +
            "or (hasAnyRole('ROLE_MANAGER', 'ROLE_SECURITY') and @passAuthFacade.isTerritoryIdMatch(#passId))")
    @PatchMapping("/{passId}/unwarning")
    public ResponseEntity<PassResponseDTO> unWarningPass(@PathVariable UUID passId) {

        PassResponseDTO completedPass = service.unWarningPass(passId);
        return ResponseEntity.ok(completedPass);
    }

    @Operation(summary = "Отметить пропуск как избранный",
            description = PASS_ACCESS_ALL_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Отмечен"),
            @ApiResponse(responseCode = "404", description = "Не найден")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @passAuthFacade.isIdMatch(#passId)")
    @PatchMapping("/{passId}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markFavorite(@PathVariable UUID passId) {
        service.markFavorite(passId);
    }

    @Operation(summary = "Отметить пропуск как НЕизбранный",
            description = PASS_ACCESS_ALL_MESSAGE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Отмечен"),
            @ApiResponse(responseCode = "404", description = "Не найден")})
    @PreAuthorize("hasRole('ROLE_ADMIN') or @passAuthFacade.isIdMatch(#passId)")
    @PatchMapping("/{passId}/not_favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unmarkFavorite(@PathVariable UUID passId) {
        service.unmarkFavorite(passId);
    }

    /* DELETE */
    @Operation(summary = "Удалить пропуск",
            description = "Доступ: ADMIN - все пропуски, MANAGER, SECURITY - свои и другие пропуска на закрепленной территории, " +
                    "USER - только свой")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пропуск успешно удален"),
            @ApiResponse(responseCode = "404", description = PASS_NOT_FOUND_MESSAGE)})
    @PreAuthorize("hasRole('ROLE_ADMIN')" +
            "or (hasAnyRole('ROLE_MANAGER', 'ROLE_SECURITY') and @passAuthFacade.isTerritoryIdMatch(#passId)) " +
            "or @passAuthFacade.isIdMatch(#passId)")
    @DeleteMapping("/{passId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePass(@PathVariable UUID passId) {
        service.deletePass(passId);
    }
}
