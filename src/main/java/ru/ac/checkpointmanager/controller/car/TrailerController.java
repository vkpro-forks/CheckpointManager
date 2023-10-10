package ru.ac.checkpointmanager.controller.car;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.ac.checkpointmanager.exception.CarTrailerNotFoundException;
import ru.ac.checkpointmanager.model.car.Trailer;
import ru.ac.checkpointmanager.service.car.TrailerService;
import ru.ac.checkpointmanager.utils.ErrorUtils;

import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("chpman/trailer")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY')")
public class TrailerController {

    private final TrailerService carTrailerService;

    @PostMapping
    public ResponseEntity<?> createTrailer(@Valid @RequestBody Trailer trailer, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = ErrorUtils.errorsList(bindingResult);
            return new ResponseEntity<>(errorMsg, HttpStatus.BAD_REQUEST);
        }
        if (carTrailerService.trailerExistsByLicensePlate(trailer.getLicensePlate())) {
            return new ResponseEntity<>("Trailer with this license plate already exists", HttpStatus.CONFLICT);
        }
        Trailer createdTrailer = carTrailerService.addTrailer(trailer);
        return new ResponseEntity<>(createdTrailer, HttpStatus.CREATED);
    }


    @GetMapping("{id}")
    public ResponseEntity<?> getTrailer(@PathVariable UUID id) {
        Trailer responseTrailer = carTrailerService.getTrailer(id);
        if (responseTrailer == null) {
            return new ResponseEntity<>("Trailer not found", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(responseTrailer, HttpStatus.OK);
    }


    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteTrailer(@PathVariable UUID id) {
        try {
            carTrailerService.deleteTrailer(id);
            return new ResponseEntity<>("Trailer " + id + " deleted!", HttpStatus.OK);
        } catch (CarTrailerNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateTrailer(@PathVariable UUID id, @RequestBody Trailer trailer) {
        try {
            Trailer updatedTrailer = carTrailerService.updateTrailer(trailer, id);
            return new ResponseEntity<>(updatedTrailer, HttpStatus.OK);
        } catch (CarTrailerNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }


    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNoSuchElementException(CarTrailerNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}

