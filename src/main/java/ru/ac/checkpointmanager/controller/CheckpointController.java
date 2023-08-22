package ru.ac.checkpointmanager.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.service.CheckpointService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("checkpoint")
public class CheckpointController {

    private final CheckpointService service;
    private final ModelMapper modelMapper;

    @Autowired
    public CheckpointController(CheckpointService service, ModelMapper modelMapper) {
        this.service = service;
        this.modelMapper = modelMapper;
    }

    /* CREATE */
    @PostMapping
    public ResponseEntity<CheckpointDTO> addCheckpoint(@RequestBody CheckpointDTO checkpointDTO) {
        Checkpoint newCheckpoint = service.addCheckpoint(convertToCheckpoint(checkpointDTO));
        return ResponseEntity.ok(convertToCheckpointDTO(newCheckpoint));
    }

    /* READ */
    @GetMapping("/{id}")
    public ResponseEntity<CheckpointDTO> getCheckpoint(@PathVariable("id") int id) {
        Checkpoint checkpoint = service.findCheckpointById(id);
        if (checkpoint == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(convertToCheckpointDTO(checkpoint));
    }

    @GetMapping("/name")
    public ResponseEntity<List<CheckpointDTO>> getCheckpointsByName(@RequestParam String name) {
        List<Checkpoint> checkpoints = service.findCheckpointsByName(name);
        if (checkpoints.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(checkpoints.stream()
                .map(this::convertToCheckpointDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping
    public ResponseEntity<List<CheckpointDTO>> getCheckpoints() {
        List<Checkpoint> checkpoints = service.findAllCheckpoints();
        if (checkpoints.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(checkpoints.stream()
                .map(this::convertToCheckpointDTO)
                .collect(Collectors.toList()));
    }

    /* UPDATE */
    @PutMapping
    public ResponseEntity<CheckpointDTO> editCheckpoint(@RequestBody CheckpointDTO checkpointDTO) {
        Checkpoint currentCheckpoint = service.findCheckpointById(checkpointDTO.getId());
        if (currentCheckpoint == null) {
            return ResponseEntity.notFound().build();
        }
        Checkpoint updatedCheckpoint = service.updateCheckpoint(convertToCheckpoint(checkpointDTO));
        return ResponseEntity.ok(convertToCheckpointDTO(updatedCheckpoint));
    }

    /* DELETE */
    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCheckpoint(@PathVariable int id) {
        Checkpoint currentCheckpoint = service.findCheckpointById(id);
        if (currentCheckpoint == null) {
            return ResponseEntity.notFound().build();
        }
        service.deleteCheckpointById(id);
        return ResponseEntity.ok().build();
    }

    /* DTO mapping */
    private Checkpoint convertToCheckpoint(CheckpointDTO checkpointDTO) {
        return modelMapper.map(checkpointDTO, Checkpoint.class);

//        example without ModelMapper:
//        Checkpoint checkpoint = new Checkpoint();

//        checkpoint.setName(checkpointDTO.getName());
//        checkpoint.setType(checkpointDTO.getType());
//        checkpoint.setNote(checkpointDTO.getNote());
//        ... a lot of other available fields

//        return checkpoint;
    }

    private CheckpointDTO convertToCheckpointDTO(Checkpoint checkpoint) {
        return modelMapper.map(checkpoint, CheckpointDTO.class);
    }

}
