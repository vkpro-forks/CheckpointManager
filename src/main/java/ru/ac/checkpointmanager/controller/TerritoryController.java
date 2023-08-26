package ru.ac.checkpointmanager.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.service.TerritoryService;

import jakarta.validation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("territory")
public class TerritoryController {

    private final TerritoryService service;
    private final ModelMapper modelMapper;

    @Autowired
    public TerritoryController(TerritoryService service, ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    /* CREATE */
    @PostMapping
    public ResponseEntity<?> addTerritory(@RequestBody @Valid TerritoryDTO territoryDTO,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Territory newTerritory = service.addTerritory(convertToTerritory(territoryDTO));
        return ResponseEntity.ok(convertToTerritoryDTO(newTerritory));
    }

    /* READ */
    @GetMapping("/{id}")
    public ResponseEntity<TerritoryDTO> getTerritory(@PathVariable("id") int id) {
        Territory territory = service.findTerritoryById(id);
        if (territory == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToTerritoryDTO(territory));
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
            return new ResponseEntity<>(errorsList(bindingResult), HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<Void> deleteTerritory(@PathVariable int id) {
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

    private String errorsList(BindingResult bindingResult) {
        StringBuilder errorMsg = new StringBuilder();
        List<FieldError> errors = bindingResult.getFieldErrors();
        for (FieldError error : errors) {
            errorMsg.append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append("\n");
        }
        return errorMsg.toString();
    }
}