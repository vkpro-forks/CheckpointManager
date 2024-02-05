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
import ru.ac.checkpointmanager.utils.SwaggerConstants;

import java.util.UUID;

import static ru.ac.checkpointmanager.utils.SwaggerConstants.INTERNAL_SERVER_ERROR_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UNAUTHORIZED_MSG;

@RestController
@RequestMapping("api/v1/pass")
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
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пропуск успешно добавлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PassResponseDTO addPass(@RequestBody @Valid PassCreateDTO passCreateDTO) {
        return service.addPass(passCreateDTO);
    }

    /* READ */
    @Operation(summary = "Получить список всех пропусков, с учетом фильтрации и совпадению" +
            " по первым буквам посетителя или номера авто",
            description = SwaggerConstants.ACCESS_ADMIN_MESSAGE,
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
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping
    public Page<PassResponseDTO> getPasses(@Schema(hidden = true) @Valid @PagingParam PagingParams pagingParams,
                                           @Schema(hidden = true) FilterParams filterParams,
                                           @Schema(hidden = true)
                                           @RequestParam(value = "part", required = false) String part) {
        return service.findPasses(pagingParams, filterParams, part);
    }

    @Operation(summary = "Найти пропуск по id",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск найден",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @GetMapping("/{id}")
    public PassResponseDTO getPass(@PathVariable("id") UUID id) {
        return service.findById(id);
    }

    @Operation(summary = "Получить список пропусков конкретного пользователя",
            description = "Доступ: ADMIN, USER.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page"),
                    @Parameter(in = ParameterIn.QUERY, name = "size"),
                    @Parameter(in = ParameterIn.QUERY, name = "dtype"),
                    @Parameter(in = ParameterIn.QUERY, name = "territory"),
                    @Parameter(in = ParameterIn.QUERY, name = "status"),
                    @Parameter(in = ParameterIn.QUERY, name = "favorite")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}")
    public Page<PassResponseDTO> getPassesByUserId(@PathVariable UUID userId,
                                                   @Schema(hidden = true) @Valid @PagingParam PagingParams pagingParams,
                                                   @Schema(hidden = true) FilterParams filterParams) {
        return service.findPassesByUser(userId, pagingParams, filterParams);
    }

    @Operation(summary = "Получить список пропусков на конкретную территорию",
            description = "Доступ: ADMIN, MANAGER, SECURITY.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page"),
                    @Parameter(in = ParameterIn.QUERY, name = "size"),
                    @Parameter(in = ParameterIn.QUERY, name = "dtype"),
                    @Parameter(in = ParameterIn.QUERY, name = "territory"),
                    @Parameter(in = ParameterIn.QUERY, name = "status"),
                    @Parameter(in = ParameterIn.QUERY, name = "favorite")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Территория не найдена")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/territory/{territoryId}")
    public Page<PassResponseDTO> getPassesByTerritoryId(@PathVariable UUID territoryId,
                                                        @Schema(hidden = true)
                                                        @Valid @PagingParam PagingParams pagingParams,
                                                        @Schema(hidden = true) FilterParams filterParams) {
        return service.findPassesByTerritory(territoryId, pagingParams, filterParams);
    }

    @Operation(summary = "Получить список пропусков по всем привязанным к пользователю территориям",
            description = "Доступ: ADMIN, MANAGER.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page"),
                    @Parameter(in = ParameterIn.QUERY, name = "size"),
                    @Parameter(in = ParameterIn.QUERY, name = "dtype"),
                    @Parameter(in = ParameterIn.QUERY, name = "territory"),
                    @Parameter(in = ParameterIn.QUERY, name = "status"),
                    @Parameter(in = ParameterIn.QUERY, name = "favorite")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Пользователь или территории не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/user/{userId}/territories")
    public Page<PassResponseDTO> getPassesByUsersTerritories(@PathVariable UUID userId,
                                                             @Schema(hidden = true)
                                                             @Valid @PagingParam PagingParams pagingParams,
                                                             @Schema(hidden = true) FilterParams filterParams) {
        return service.findPassesByUsersTerritories(userId, pagingParams, filterParams);
    }

    /* UPDATE */
    @Operation(summary = "Изменить существующий пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно изменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PutMapping
    public PassResponseDTO updatePass(@RequestBody @Valid PassUpdateDTO passUpdateDTO) {
        return service.updatePass(passUpdateDTO);
    }

    @Operation(summary = "Отменить активный пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск отменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Пропуск не является активным"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PassResponseDTO> cancelPass(@PathVariable UUID id) {

        PassResponseDTO cancelledPass = service.cancelPass(id);
        return ResponseEntity.ok(cancelledPass);
    }

    @Operation(summary = "Активировать отмененный пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск активирован",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Пропуск не является отмененным; время действия пропуска истекло"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<PassResponseDTO> activatePass(@PathVariable UUID id) {

        PassResponseDTO activatedPass = service.activateCancelledPass(id);
        return ResponseEntity.ok(activatedPass);
    }

    @Operation(summary = "Отметить выполненным пропуск со статусом Warning (время истекло, последнее пересечение на выезд)",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск отмечен выполненным",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Стутус отличен от Warning"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("/{id}/unwarning")
    public ResponseEntity<PassResponseDTO> unWarningPass(@PathVariable UUID id) {

        PassResponseDTO completedPass = service.unWarningPass(id);
        return ResponseEntity.ok(completedPass);
    }

    @Operation(summary = "Отметить пропуск как избранный",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Отмечен"),
            @ApiResponse(responseCode = "404", description = "Не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("/{id}/favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markFavorite(@PathVariable UUID id) {
        service.markFavorite(id);
    }

    @Operation(summary = "Отметить пропуск как НЕизбранный",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Отмечен"),
            @ApiResponse(responseCode = "404", description = "Не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("/{id}/not_favorite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unmarkFavorite(@PathVariable UUID id) {
        service.unmarkFavorite(id);
    }

    /* DELETE */
    @Operation(summary = "Удалить пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пропуск успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePass(@PathVariable UUID id) {
        service.deletePass(id);
    }
}
