package ru.ac.checkpointmanager.controller;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.TerritoryService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import jakarta.validation.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("territory")
@RequiredArgsConstructor
public class TerritoryController {

    private final TerritoryService service;
    private final ModelMapper modelMapper;

    /* CREATE */
    @PostMapping
    public ResponseEntity<?> addTerritory(@RequestBody @Valid TerritoryDTO territoryDTO,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Territory newTerritory = service.addTerritory(convertToTerritory(territoryDTO));
        return ResponseEntity.ok(convertToTerritoryDTO(newTerritory));
    }

    /* READ */
    @GetMapping("/{territoryId}")
    public ResponseEntity<TerritoryDTO> getTerritory(@PathVariable("territoryId") UUID territoryId) {
        Territory territory = service.findTerritoryById(territoryId);
        if (territory == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToTerritoryDTO(territory));
    }

    @GetMapping("/{territoryId}/users")
    public ResponseEntity<Set<User>> getUsersByTerritory(@PathVariable UUID territoryId) {
        Set<User> users = service.findUsersByTerritoryId(territoryId);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/name")
    public ResponseEntity<List<TerritoryDTO>> getTerritoriesByName(@RequestParam String name) {
        List<Territory> territories = service.findTerritoriesByName(name);
        if (territories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(territories.stream()
                .map(this::convertToTerritoryDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<TerritoryDTO>> getTerritories() {
        List<Territory> territories = service.findAllTerritories();
        if (territories.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(territories.stream()
                .map(this::convertToTerritoryDTO)
                .collect(Collectors.toList()));
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
        Territory updatedTerritory = service.updateTerritory(convertToTerritory(territoryDTO));
        return ResponseEntity.ok(convertToTerritoryDTO(updatedTerritory));
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

    /* DTO mapping */
    private Territory convertToTerritory(TerritoryDTO territoryDTO) {
        return modelMapper.map(territoryDTO, Territory.class);
    }

    private TerritoryDTO convertToTerritoryDTO(Territory territory) {
        return modelMapper.map(territory, TerritoryDTO.class);
    }
}