package ru.ac.checkpointmanager.controller;

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
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.territories.TerritoryService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("chpman/territory")
@RequiredArgsConstructor
@Tag(name = "Territory (территория)", description = "Администрирование списка территорий")
@ApiResponses(value = {@ApiResponse(responseCode = "401", description = "UNAUTHORIZED: пользователь не авторизован"),
        @ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR: Ошибка сервера при обработке запроса")})
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class TerritoryController {

    private final TerritoryService service;
    private final Mapper mapper;

    /* CREATE */
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
    @GetMapping("/{territoryId}")
    public ResponseEntity<TerritoryDTO> getTerritory(@PathVariable("territoryId") UUID territoryId) {
        Territory territory = service.findTerritoryById(territoryId);

        return ResponseEntity.ok(mapper.toTerritoryDTO(territory));
    }

    @GetMapping("/{territoryId}/users")
    public ResponseEntity<List<UserDTO>> getUsersByTerritory(@PathVariable UUID territoryId) {
        List<User> users = service.findUsersByTerritoryId(territoryId);

        return ResponseEntity.ok(mapper.toUsersDTO(users));
    }

    @GetMapping("/name")
    public ResponseEntity<List<TerritoryDTO>> getTerritoriesByName(@RequestParam String name) {
        List<Territory> territories = service.findTerritoriesByName(name);
        if (territories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toTerritoriesDTO(territories));
    }

    @GetMapping
    public ResponseEntity<List<TerritoryDTO>> getTerritories() {
        List<Territory> territories = service.findAllTerritories();
        if (territories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toTerritoriesDTO(territories));
    }

    /* UPDATE */
    @PutMapping
    public ResponseEntity<?> editTerritory(@RequestBody @Valid TerritoryDTO territoryDTO,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Territory updatedTerritory = service.updateTerritory(mapper.toTerritory(territoryDTO));
        return ResponseEntity.ok(mapper.toTerritoryDTO(updatedTerritory));
    }

    @PatchMapping("/{territoryId}/user/{userId}")
    public ResponseEntity<?> attachUserToTerritory(@PathVariable UUID territoryId,
                                                 @PathVariable UUID userId) {

        service.attachUserToTerritory(territoryId, userId);
        return ResponseEntity.ok().build();
    }

    /* DELETE */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteTerritory(@PathVariable UUID id) {

        service.deleteTerritoryById(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{territoryId}/user/{userId}")
    public ResponseEntity<?> detachUserFromTerritory(@PathVariable UUID territoryId,
                                                 @PathVariable UUID userId) {

        service.detachUserFromTerritory(territoryId, userId);
        return ResponseEntity.ok().build();
    }
}