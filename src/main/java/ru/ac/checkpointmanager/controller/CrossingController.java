package ru.ac.checkpointmanager.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.model.enums.Direction;
import ru.ac.checkpointmanager.service.CrossingService;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/crossing")
@RequiredArgsConstructor
//я предполагаю, что этот эндпоинт будет вызываться когда будет открываться шлагбаум(например) и тем самым фиксироваться пересечение
public class CrossingController {

    private final CrossingService crossingService;

    @PostMapping("/mark")
    public ResponseEntity<Crossing> markCrossing(@RequestParam UUID passId,
                                                 @RequestParam Checkpoint checkpoint,
                                                 @RequestParam LocalDateTime localDateTime,
                                                 @RequestParam Direction direction) {
        Crossing crossing = crossingService.markCrossing(passId, checkpoint, localDateTime, direction);
        return new ResponseEntity<>(crossing, HttpStatus.OK);
    }
}
