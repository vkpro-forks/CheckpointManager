package ru.ac.checkpointmanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.TerritoryService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import jakarta.validation.*;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("territory")
@RequiredArgsConstructor
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
        if (territory == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toTerritoryDTO(territory));
    }

    @GetMapping("/{territoryId}/users")
    public ResponseEntity<List<UserDTO>> getUsersByTerritory(@PathVariable UUID territoryId) {
        List<User> users = service.findUsersByTerritoryId(territoryId);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
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

        Territory currentTerritory = service.findTerritoryById(territoryDTO.getId());
        if (currentTerritory == null) {
            return ResponseEntity.notFound().build();
        }
        Territory updatedTerritory = service.updateTerritory(mapper.toTerritory(territoryDTO));
        return ResponseEntity.ok(mapper.toTerritoryDTO(updatedTerritory));
    }

    @PatchMapping("/{territoryId}/user/{userId}")
    public ResponseEntity<?> joinUserToTerritory(@PathVariable UUID territoryId,
                                                 @PathVariable UUID userId) {

        service.joinUserToTerritory(territoryId, userId);
        return ResponseEntity.ok().build();
    }

    /* DELETE */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteTerritory(@PathVariable UUID id) {
        Territory currentTerritory = service.findTerritoryById(id);
        if (currentTerritory == null) {
            return ResponseEntity.notFound().build();
        }
        service.deleteTerritoryById(id);
        return ResponseEntity.ok().build();
    }
}