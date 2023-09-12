package ru.ac.checkpointmanager.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.utils.Mapper;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.service.CheckpointService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import jakarta.validation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("checkpoint")
@RequiredArgsConstructor
public class CheckpointController {

    private final CheckpointService service;
    private final Mapper mapper;

    /* CREATE */
    @PostMapping
    public ResponseEntity<?> addCheckpoint(@RequestBody @Valid ru.ac.checkpointmanager.dto.CheckpointDTO checkpointDTO,
                                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Checkpoint newCheckpoint = service.addCheckpoint(mapper.toCheckpoint(checkpointDTO));
        return ResponseEntity.ok(mapper.toCheckpointDTO(newCheckpoint));
    }

    /* READ */
    @GetMapping("/{id}")
    public ResponseEntity<CheckpointDTO> getCheckpoint(@PathVariable("id") UUID id) {
        Checkpoint foundCheckpoint = service.findCheckpointById(id);
        if (foundCheckpoint == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toCheckpointDTO(foundCheckpoint));
    }

    @GetMapping("/name")
    public ResponseEntity<List<CheckpointDTO>> getCheckpointsByName(@RequestParam String name) {
        List<Checkpoint> checkpoints = service.findCheckpointsByName(name);
        if (checkpoints.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toCheckpointsDTO(checkpoints));
    }

    @GetMapping
    public ResponseEntity<List<CheckpointDTO>> getCheckpoints() {
        List<Checkpoint> checkpoints = service.findAllCheckpoints();
        if (checkpoints.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toCheckpointsDTO(checkpoints));
    }

    @GetMapping("/territory")
    public ResponseEntity<List<CheckpointDTO>> getCheckpointsByTerritoryId(@RequestParam UUID id) {
        List<Checkpoint> checkpoints = service.findCheckpointsByTerritoryId(id);
        if (checkpoints.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toCheckpointsDTO(checkpoints));
    }

    /* UPDATE */
    @PutMapping
    public ResponseEntity<?> editCheckpoint(@RequestBody @Valid CheckpointDTO checkpointDTO,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Checkpoint currentCheckpoint = service.findCheckpointById(checkpointDTO.getId());
        if (currentCheckpoint == null) {
            return ResponseEntity.notFound().build();
        }
        Checkpoint updatedCheckpoint = service.updateCheckpoint(mapper.toCheckpoint(checkpointDTO));
        return ResponseEntity.ok(mapper.toCheckpointDTO(updatedCheckpoint));
    }

    /* DELETE */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCheckpoint(@PathVariable UUID id) {
        Checkpoint currentCheckpoint = service.findCheckpointById(id);
        if (currentCheckpoint == null) {
            return ResponseEntity.notFound().build();
        }
        service.deleteCheckpointById(id);
        return ResponseEntity.ok().build();
    }
}
