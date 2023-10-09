package ru.ac.checkpointmanager.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.service.CrossingService;
import ru.ac.checkpointmanager.utils.ErrorUtils;
import ru.ac.checkpointmanager.utils.Mapper;


@RestController
@RequestMapping("chpman/crossing")
@RequiredArgsConstructor
//я предполагаю, что этот эндпоинт будет вызываться когда будет открываться шлагбаум(например) и тем самым фиксироваться пересечение
public class CrossingController {

    private final CrossingService crossingService;
    private final Mapper mapper;

    @PostMapping("/mark")
    public ResponseEntity<?> markCrossing(@Valid @RequestBody CrossingDTO crossingDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }

        Crossing crossing = crossingService.markCrossing(mapper.toCrossing(crossingDTO));

        return new ResponseEntity<>(mapper.toCrossingDTO(crossing), HttpStatus.OK);
    }
}
