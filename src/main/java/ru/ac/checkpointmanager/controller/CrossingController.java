package ru.ac.checkpointmanager.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ac.checkpointmanager.dto.CrossingDTO;
import ru.ac.checkpointmanager.model.Crossing;
import ru.ac.checkpointmanager.service.CrossingService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import static ru.ac.checkpointmanager.utils.Mapper.toCrossing;
import static ru.ac.checkpointmanager.utils.Mapper.toCrossingDTO;


@RestController
@RequestMapping("chpman/crossing")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
//я предполагаю, что этот эндпоинт будет вызываться когда будет открываться шлагбаум(например) и тем самым фиксироваться пересечение
public class CrossingController {

    private final CrossingService crossingService;

    @PostMapping("/mark")
    public ResponseEntity<?> markCrossing(@Valid @RequestBody CrossingDTO crossingDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>(ErrorUtils.errorsList(bindingResult), HttpStatus.BAD_REQUEST);
        }
        Crossing crossing = crossingService.markCrossing(toCrossing(crossingDTO));
        return new ResponseEntity<>(toCrossingDTO(crossing), HttpStatus.OK);
    }
}
