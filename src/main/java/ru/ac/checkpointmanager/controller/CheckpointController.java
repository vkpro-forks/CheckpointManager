package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.service.checkpoints.CheckpointService;

import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.SwaggerConstants.FAILED_FIELD_VALIDATION_MESSAGE;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.INTERNAL_SERVER_ERROR_MSG;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.KPP_NOT_FOUND_PLURAL;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.KPP_NOT_FOUND_SINGULAR;
import static ru.ac.checkpointmanager.utils.SwaggerConstants.UNAUTHORIZED_MSG;

@RestController
@RequestMapping("api/v1/checkpoints")
@Validated
@RequiredArgsConstructor
@Tag(name = "Checkpoint (КПП)", description = "Администрирование списка КПП для обслуживаемых территорий")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = UNAUTHORIZED_MSG),
        @ApiResponse(responseCode = "500", description = INTERNAL_SERVER_ERROR_MSG)})
@SecurityRequirement(name = "bearerAuth")
public class CheckpointController {

    private final CheckpointService checkpointService;

    /* CREATE */
    @Operation(summary = "Добавить новый КПП",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "КПП успешно добавлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckpointDTO.class))}),
            @ApiResponse(responseCode = "400", description = FAILED_FIELD_VALIDATION_MESSAGE)})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckpointDTO addCheckpoint(@RequestBody @Valid CheckpointDTO checkpointDTO) {
        return checkpointService.addCheckpoint(checkpointDTO);
    }

    /* READ */
    @Operation(summary = "Найти КПП по id",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП найден",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckpointDTO.class))}),
            @ApiResponse(responseCode = "404", description = KPP_NOT_FOUND_SINGULAR)})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @GetMapping("/{checkpointId}")
    public ResponseEntity<CheckpointDTO> getCheckpoint(@PathVariable UUID checkpointId) {
        CheckpointDTO foundCheckpoint = checkpointService.findById(checkpointId);
        return ResponseEntity.ok(foundCheckpoint);
    }

    @Operation(summary = "Найти список КПП по названию",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CheckpointDTO.class)))),
            @ApiResponse(responseCode = "404", description = KPP_NOT_FOUND_PLURAL)})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @GetMapping("/name")
    public List<CheckpointDTO> getCheckpointsByName(@Parameter(description = "Часть названия")
                                                    @RequestParam String name) {
        return checkpointService.findCheckpointsByName(name);
    }

    @Operation(summary = "Получить список всех КПП",
            description = "Доступ: ADMIN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CheckpointDTO.class)))),
            @ApiResponse(responseCode = "404", description = KPP_NOT_FOUND_PLURAL)})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping
    public List<CheckpointDTO> getCheckpoints() {
        return checkpointService.findAllCheckpoints();
    }

    @Operation(summary = "Получить список КПП, привязанных к указанной территории",
            description = "Доступ: ADMIN - любые территории, MANAGER, SECURITY, USER - только свои")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CheckpointDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Не найдена указанная территория"),
            @ApiResponse(responseCode = "404", description = KPP_NOT_FOUND_PLURAL)})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN') or @territoryAuthFacade.isIdMatch(#territoryId)")
    @GetMapping("/territories/{territoryId}")
    public List<CheckpointDTO> getCheckpointsByTerritoryId(@Parameter(description = "ID территории")
                                                           @PathVariable UUID territoryId) {
        return checkpointService.findCheckpointsByTerritoryId(territoryId);
    }

    /* UPDATE */
    @Operation(summary = "Изменить КПП",
            description = "Доступ: ADMIN - любые территории, MANAGER - только свои")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "КПП успешно изменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckpointDTO.class))}),
            @ApiResponse(responseCode = "400", description = FAILED_FIELD_VALIDATION_MESSAGE),
            @ApiResponse(responseCode = "404", description = KPP_NOT_FOUND_SINGULAR)})
    @PreAuthorize("hasRole('ROLE_ADMIN') or (hasRole('ROLE_MANAGER') and @checkpointAuthFacade.isIdMatch(#checkpointDTO.id))")
    @PutMapping
    public CheckpointDTO updateCheckpoint(@RequestBody @Valid CheckpointDTO checkpointDTO) {
        return checkpointService.updateCheckpoint(checkpointDTO);
    }

    /* DELETE */
    @Operation(summary = "Удалить КПП",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "КПП успешно удален"),
            @ApiResponse(responseCode = "404", description = KPP_NOT_FOUND_SINGULAR)})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("{checkpointId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCheckpoint(@PathVariable UUID checkpointId) {
        checkpointService.deleteCheckpointById(checkpointId);
    }

}
