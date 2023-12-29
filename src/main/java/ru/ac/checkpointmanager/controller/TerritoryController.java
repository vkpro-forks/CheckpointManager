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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.service.territories.TerritoryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/territory")
@Validated
@RequiredArgsConstructor
@Tag(name = "Territory (территория)", description = "Администрирование списка территорий")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
@SecurityRequirement(name = "bearerAuth")
public class TerritoryController {

    private final TerritoryService territoryService;

    /* CREATE */
    @Operation(summary = "Добавить новую территорию",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Территория успешно добавлена",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TerritoryDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидаци полей")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TerritoryDTO addTerritory(@RequestBody @Valid TerritoryDTO territoryDTO) {
        return territoryService.addTerritory(territoryDTO);
    }

    /* READ */
    @Operation(summary = "Найти территорию по id",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Территория найдена",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TerritoryDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Территория не найдена")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @GetMapping("/{territoryId}")
    public TerritoryDTO getTerritory(@PathVariable("territoryId") UUID territoryId) {
        return territoryService.findById(territoryId);
    }

    @Operation(summary = "Найти список пользователей, привязанных к территории",
            description = "Доступ: ADMIN, MANAGER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователи найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = UserResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Территория или пользователи не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @GetMapping("/{territoryId}/users")
    public List<UserResponseDTO> getUsersByTerritory(@PathVariable UUID territoryId) {
        return territoryService.findUsersByTerritoryId(territoryId);
    }

    @Operation(summary = "Найти список территорий по названию",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Территории найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TerritoryDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Территории не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @GetMapping("/name")
    public List<TerritoryDTO> getTerritoriesByName(@RequestParam String name) {
        return territoryService.findTerritoriesByName(name);
    }

    @Operation(summary = "Получить список всех территорий",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Территории найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = TerritoryDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Территории не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @GetMapping
    public List<TerritoryDTO> getTerritories() {
        return territoryService.findAllTerritories();
    }

    /* UPDATE */
    @Operation(summary = "Обновить данные территории",
            description = "Доступ: ADMIN, MANAGER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные успешно изменены",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TerritoryDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидаци полей"),
            @ApiResponse(responseCode = "404", description = "Территория не найдена")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PutMapping
    public TerritoryDTO updateTerritory(@RequestBody @Valid TerritoryDTO territoryDTO) {
        return territoryService.updateTerritory(territoryDTO);
    }

    @Operation(summary = "Прикрепить пользователя к территории (дать право создавать пропуска)",
            description = "Доступ: ADMIN, MANAGER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь присоединен"),
            @ApiResponse(responseCode = "400", description = "Указанные пользователь и территория уже соединены"),
            @ApiResponse(responseCode = "404", description = "Пользователь или территория не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/{territoryId}/user/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void attachUserToTerritory(@PathVariable UUID territoryId,
                                      @PathVariable UUID userId) {
        territoryService.attachUserToTerritory(territoryId, userId);
    }

    /* DELETE */
    @Operation(summary = "Удалить территорию и все ее КПП",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Территория удалена"),
            @ApiResponse(responseCode = "404", description = "Территория не найдена")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTerritory(@PathVariable UUID id) {
        territoryService.deleteTerritoryById(id);
    }

    @Operation(summary = "Открепить пользователя от территории (если утрачено право создавать пропуска)",
            description = "Доступ: ADMIN, MANAGER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пользователь отсоединен"),
            @ApiResponse(responseCode = "400", description = "Указанные пользователь и территория не соединены"),
            @ApiResponse(responseCode = "404", description = "Пользователь или территория не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @DeleteMapping("/{territoryId}/user/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detachUserFromTerritory(@PathVariable UUID territoryId,
                                        @PathVariable UUID userId) {
        territoryService.detachUserFromTerritory(territoryId, userId);
    }
}