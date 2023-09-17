package ru.ac.checkpointmanager.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.PassDTO;
import ru.ac.checkpointmanager.model.Pass;
import ru.ac.checkpointmanager.service.PassService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("pass")
@RequiredArgsConstructor
public class PassController {

    private final PassService service;
    private final Mapper mapper;

    /* CREATE */
    @PostMapping
    public ResponseEntity<?> addPass(@RequestBody @Valid PassDTO passDTO,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Pass newPass = service.addPass(mapper.toPass(passDTO));
        return ResponseEntity.ok(mapper.toPassDTO(newPass));
    }

    /* READ */
    @GetMapping
    public ResponseEntity<List<PassDTO>> getPasses() {
        List<Pass> passes = service.findPasses();
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(passes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassDTO> getPass(@PathVariable("id") UUID id) {
        Pass foundPass = service.findPass(id);
        if (foundPass == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(foundPass));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PassDTO>> getPassesByUserId(@PathVariable UUID userId) {
        List<Pass> passes = service.findPassesByUser(userId);
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(passes));
    }

    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<List<PassDTO>> getPassesByTerritoryId(@PathVariable UUID territoryId) {
        List<Pass> passes = service.findPassesByTerritory(territoryId);
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toPassDTO(passes));
    }

    /* UPDATE */
    @PutMapping
    public ResponseEntity<?> editPass(@RequestBody @Valid PassDTO passDTO,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Pass currentPass = service.findPass(passDTO.getId());
        if (currentPass == null) {
            return ResponseEntity.notFound().build();
        }
        Pass updatedPass = service.updatePass(mapper.toPass(passDTO));
        return ResponseEntity.ok(mapper.toPassDTO(updatedPass));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PassDTO> cancelPass(@PathVariable UUID id) {

        Pass cancelledPass = service.cancelPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(cancelledPass));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PassDTO> activatePass(@PathVariable UUID id) {

        Pass activatedPass = service.activateCancelledPass(id);
        return ResponseEntity.ok(mapper.toPassDTO(activatedPass));
    }

    /* DELETE */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deletePass(@PathVariable UUID id) {
        Pass currentPass = service.findPass(id);
        if (currentPass == null) {
            return ResponseEntity.notFound().build();
        }
        service.deletePass(id);
        return ResponseEntity.ok().build();
    }
}
