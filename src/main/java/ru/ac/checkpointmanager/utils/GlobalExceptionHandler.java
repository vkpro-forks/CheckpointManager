package ru.ac.checkpointmanager.utils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.ac.checkpointmanager.exception.CarBrandNotFoundException;
import ru.ac.checkpointmanager.exception.EntranceWasAlreadyException;

import ru.ac.checkpointmanager.exception.InactivePassException;

import ru.ac.checkpointmanager.exception.TerritoryNotFoundException;
import ru.ac.checkpointmanager.service.PassServiceImpl;


import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(PassServiceImpl.class);

    @ExceptionHandler(CarBrandNotFoundException.class)
    public ResponseEntity<String> handleCarBrandNotFoundException(CarBrandNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolationException(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());
        return new ResponseEntity<>(new ApiError(HttpStatus.BAD_REQUEST, "Validation errors", errors),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFoundException(EntityNotFoundException ex) {
        return new ResponseEntity<>(new ApiError(HttpStatus.NOT_FOUND, ex.getMessage(),
                (List<String>) null), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex) {
        return new ResponseEntity<>(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
                (List<String>) null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntranceWasAlreadyException.class)
    public ResponseEntity<String> handleEntranceWasAlreadyException(EntranceWasAlreadyException e) {
        System.out.println("Handling EntranceWasAlreadyException");
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InactivePassException.class)
    public ResponseEntity<String> handleNoActivePassException(InactivePassException e) {
        System.out.println("Handling NoActivePassException");
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TerritoryNotFoundException.class)
    public ResponseEntity<String> handleTerritoryNotFoundException(TerritoryNotFoundException e) {
        logger.error("Handling TerritoryNotFoundException");
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        logger.error(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}
