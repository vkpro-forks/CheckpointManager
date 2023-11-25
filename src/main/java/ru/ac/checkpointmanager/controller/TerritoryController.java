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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.territories.TerritoryService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/territory")
@RequiredArgsConstructor
@Tag(name = "Territory (территория)", description = "Администрирование списка территорий")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
@SecurityRequirement(name = "bearerAuth")
public class TerritoryController {

    private final TerritoryService service;
    private final Mapper mapper;

    /* CREATE */
    @Operation(summary = "Добавить новую территорию",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Территория успешно добавлена",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = TerritoryDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидаци полей")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<?> addTerritory(@RequestBody @Valid TerritoryDTO territoryDTO,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Territory newTerritory = service.addTerritory(mapper.toTerritory(territoryDTO));
        return ResponseEntity.ok(mapper.toTerritoryDTO(newTerritory));
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
    public ResponseEntity<TerritoryDTO> getTerritory(@PathVariable("territoryId") UUID territoryId) {
        Territory territory = service.findTerritoryById(territoryId);

        return ResponseEntity.ok(mapper.toTerritoryDTO(territory));
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
    public ResponseEntity<List<UserResponseDTO>> getUsersByTerritory(@PathVariable UUID territoryId) {
        List<User> users = service.findUsersByTerritoryId(territoryId);

        return ResponseEntity.ok(mapper.toUsersDTO(users));
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
    public ResponseEntity<List<TerritoryDTO>> getTerritoriesByName(@RequestParam String name) {
        List<Territory> territories = service.findTerritoriesByName(name);
        if (territories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toTerritoriesDTO(territories));
    }

    @Operation(summary = "Получить список всех территорий",
            description = "Доступ: ADMIN, MANAGER, SECURITY, USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Территории найдены",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = CheckpointDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Территории не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
    @GetMapping
    public ResponseEntity<List<TerritoryDTO>> getTerritories() {
        List<Territory> territories = service.findAllTerritories();
        if (territories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toTerritoriesDTO(territories));
    }

    /* UPDATE */
    @Operation(summary = "Обновить данные территории",
            description = "Доступ: ADMIN, MANAGER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные успешно изменены",
                    content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckpointDTO.class))}),
            @ApiResponse(responseCode = "400", description = "Неуспешная валидаци полей"),
            @ApiResponse(responseCode = "404", description = "Территория не найдена")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PutMapping
    public ResponseEntity<?> editTerritory(@RequestBody @Valid TerritoryDTO territoryDTO,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Territory updatedTerritory = service.updateTerritory(mapper.toTerritory(territoryDTO));
        return ResponseEntity.ok(mapper.toTerritoryDTO(updatedTerritory));
    }

    @Operation(summary = "Прикрепить пользователя к территории (дать право создавать пропуска)",
            description = "Доступ: ADMIN, MANAGER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь присоединен"),
            @ApiResponse(responseCode = "400", description = "Указанные пользователь и территория уже соединены"),
            @ApiResponse(responseCode = "404", description = "Пользователь или территория не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @PatchMapping("/{territoryId}/user/{userId}")
    public ResponseEntity<?> attachUserToTerritory(@PathVariable UUID territoryId,
                                                 @PathVariable UUID userId) {

        service.attachUserToTerritory(territoryId, userId);
        return ResponseEntity.ok().build();
    }

    /* DELETE */
    @Operation(summary = "Удалить территорию",
            description = "Доступ: ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Территория удалена"),
            @ApiResponse(responseCode = "404", description = "Территория не найдена")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteTerritory(@PathVariable UUID id) {

        service.deleteTerritoryById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Открепить пользователя от территории (если утрачено право создавать пропуска)",
            description = "Доступ: ADMIN, MANAGER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь отсоединен"),
            @ApiResponse(responseCode = "400", description = "Указанные пользователь и территория не соединены"),
            @ApiResponse(responseCode = "404", description = "Пользователь или территория не найдены")})
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    @DeleteMapping("/{territoryId}/user/{userId}")
    public ResponseEntity<?> detachUserFromTerritory(@PathVariable UUID territoryId,
                                                 @PathVariable UUID userId) {

        service.detachUserFromTerritory(territoryId, userId);
        return ResponseEntity.ok().build();
    }
}