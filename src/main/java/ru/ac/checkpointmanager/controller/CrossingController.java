package ru.ac.checkpointmanager.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.service.CrossingService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/crossing")
@RequiredArgsConstructor
//я предполагаю, что этот эндпоинт будет вызываться когда будет открываться шлагбаум(например) и тем самым фиксироваться пересечение
public class CrossingController {

    private final CrossingService crossingService;

    @PostMapping("/mark")
    public ResponseEntity<?> markCrossing(@RequestBody CrossingDTO crossingDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Crossing crossing = crossingService.markCrossing(crossingDTO.getPassId(), crossingDTO.getCheckpoint(),
                crossingDTO.getLocalDateTime(), crossingDTO.getDirection());

        return new ResponseEntity<>(crossing, HttpStatus.OK);
    }
}
