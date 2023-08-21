package ru.ac.checkpointmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.service.CheckpointService;

@RestController
@RequestMapping("checkpoint")
public class CheckpointController {

    private final CheckpointService service;

    public CheckpointController(CheckpointService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Void> addCheckpoint(@RequestBody Checkpoint checkpoint) {
        service.addCheckpoint(checkpoint);
        return ResponseEntity.ok().build();
    }

}
