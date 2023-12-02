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
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.annotation.PagingParam;
import ru.ac.checkpointmanager.dto.passes.PagingParams;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassResponseDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.service.passes.PassService;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/pass")
@RequiredArgsConstructor
@Validated
@Tag(name = "Pass (пропуска)", description = "Операции с пропусками для машин и людей")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
@SecurityRequirement(name = "bearerAuth")
public class PassController {

    private final PassService service;

    /* CREATE */
    @Operation(summary = "Добавить новый пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно добавлен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидация полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PostMapping
    public PassResponseDTO addPass(@RequestBody @Valid PassCreateDTO passCreateDTO) {
        PassResponseDTO newPass = service.addPass(passCreateDTO);
        return newPass;
    }

    /* READ */
    @Operation(summary = "Получить список всех пропусков",
            description = "Доступ: ADMIN.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<PassResponseDTO>> getPasses(@Schema(hidden = true)
                                                           @Valid @PagingParam PagingParams pagingParams) {

        Page<PassResponseDTO> passPage = service.findPasses(pagingParams);
        return ResponseEntity.ok(passPage);
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
    public ResponseEntity<PassResponseDTO> getPass(@PathVariable("id") UUID id) {
        PassResponseDTO foundPass = service.findById(id);
        return ResponseEntity.ok(foundPass);
    }

    @Operation(summary = "Получить список пропусков конкретного пользователя",
            description = "Доступ: ADMIN, USER.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены; пользователь не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PassResponseDTO>> getPassesByUserId(@PathVariable UUID userId, @Schema(hidden = true)
    @Valid @PagingParam PagingParams pagingParams) {
        Page<PassResponseDTO> passPage = service.findPassesByUser(userId, pagingParams);
        return ResponseEntity.ok(passPage);
    }

    @Operation(summary = "Получить список пропусков на конкретную территорию",
            description = "Доступ: ADMIN, MANAGER, SECURITY.",
            parameters = {
                    @Parameter(in = ParameterIn.QUERY, name = "page", example = "0"),
                    @Parameter(in = ParameterIn.QUERY, name = "size", example = "20")
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуска найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PassResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Пропуска не найдены; территория не найдена")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<Page<PassResponseDTO>> getPassesByTerritoryId(@PathVariable UUID territoryId, @Schema(hidden = true)
    @Valid @PagingParam PagingParams pagingParams) {

        Page<PassResponseDTO> passPage = service.findPassesByTerritory(territoryId, pagingParams);
        return ResponseEntity.ok(passPage);
    }

    /* UPDATE */
    @Operation(summary = "Изменить существующий пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно изменен",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PassResponseDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидаци полей; пользователь не имеет права " +
                    "создавать пропуск на эту территорию; у пользователя найден накладывающийся пропуск"),
            @ApiResponse(responseCode = "404", description = "Не найден пользователь или территория")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PutMapping
    public PassResponseDTO updatePass(@RequestBody @Valid PassUpdateDTO passUpdateDTO) {
        PassResponseDTO updatedPass = service.updatePass(passUpdateDTO);
        return updatedPass;
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
            @ApiResponse(responseCode = "200", description = "Отмечен"),
            @ApiResponse(responseCode = "404", description = "Не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("{id}/favorite")
    public ResponseEntity<Void> markFavorite(@PathVariable UUID id) {
        service.markFavorite(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отметить пропуск как НЕизбранный",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Отмечен"),
            @ApiResponse(responseCode = "404", description = "Не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @PatchMapping("{id}/not_favorite")
    public ResponseEntity<Void> unmarkFavorite(@PathVariable UUID id) {
        service.unmarkFavorite(id);
        return ResponseEntity.ok().build();
    }

    /* DELETE */
    @Operation(summary = "Удалить пропуск",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пропуск успешно удален"),
            @ApiResponse(responseCode = "404", description = "Пропуск не найден")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deletePass(@PathVariable UUID id) {
        service.deletePass(id);
        return ResponseEntity.ok().build();
    }
}
