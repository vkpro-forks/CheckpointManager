package ru.ac.checkpointmanager.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.PassDTO;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.service.passes.PassService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.List;
import java.util.UUID;

import static ru.ac.checkpointmanager.utils.Mapper.*;

@RestController
@RequestMapping("chpman/pass")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class PassController {

    private final PassService service;

    /* CREATE */
    @PostMapping
    public ResponseEntity<?> addPass(@RequestBody @Valid PassDTO passDTO,
                                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Pass pass = toPass(passDTO);
        Pass newPass = service.addPass(pass);

        return ResponseEntity.ok(toPassDTO(newPass));
    }

    /* READ */
    @GetMapping
    public ResponseEntity<List<PassDTO>> getPasses() {
        List<Pass> passes = service.findPasses();
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toPassDTO(passes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassDTO> getPass(@PathVariable("id") UUID id) {
        Pass foundPass = service.findPass(id);
        if (foundPass == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toPassDTO(foundPass));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PassDTO>> getPassesByUserId(@PathVariable UUID userId) {
        List<Pass> passes = service.findPassesByUser(userId);
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toPassDTO(passes));
    }

    @GetMapping("/territory/{territoryId}")
    public ResponseEntity<List<PassDTO>> getPassesByTerritoryId(@PathVariable UUID territoryId) {
        List<Pass> passes = service.findPassesByTerritory(territoryId);
        if (passes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toPassDTO(passes));
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
        Pass updatedPass = service.updatePass(toPass(passDTO));
        return ResponseEntity.ok(toPassDTO(updatedPass));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<PassDTO> cancelPass(@PathVariable UUID id) {

        Pass cancelledPass = service.cancelPass(id);
        return ResponseEntity.ok(toPassDTO(cancelledPass));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<PassDTO> activatePass(@PathVariable UUID id) {

        Pass activatedPass = service.activateCancelledPass(id);
        return ResponseEntity.ok(toPassDTO(activatedPass));
    }

    @PatchMapping("/{id}/unwarning")
    public ResponseEntity<PassDTO> unWarningPass(@PathVariable UUID id) {

        Pass completedPass = service.unWarningPass(id);
        return ResponseEntity.ok(toPassDTO(completedPass));
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
