package ru.ac.checkpointmanager.utils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.ac.checkpointmanager.exception.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

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
        log.warn("Handling EntranceWasAlreadyException");
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InactivePassException.class)
    public ResponseEntity<String> handleNoActivePassException(InactivePassException e) {
        log.warn("Handling NoActivePassException");
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TerritoryNotFoundException.class)
    public ResponseEntity<String> handleTerritoryNotFoundException(TerritoryNotFoundException e) {
        log.warn("Handling TerritoryNotFoundException");
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AvatarIsTooBigException.class)
    public ResponseEntity<ApiError> handleAvatarIsTooBigException(AvatarIsTooBigException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(new ApiError(HttpStatus.BAD_REQUEST, message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AvatarNotFoundException.class)
    public ResponseEntity<ApiError> handleAvatarNotFoundException(AvatarNotFoundException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(new ApiError(HttpStatus.NOT_FOUND, message), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDeniedException(AccessDeniedException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DateOfBirthFormatException.class)
    public ResponseEntity<String> handleDateOfBirthFormatException(DateOfBirthFormatException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PhoneAlreadyExistException.class)
    public ResponseEntity<String> handlePhoneAlreadyExistException(PhoneAlreadyExistException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PhoneNumberNotFoundException.class)
    public ResponseEntity<String> handlePhoneNumberNotFoundException(PhoneNumberNotFoundException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PersonNotFoundException.class)
    public ResponseEntity<String> handlePersonNotFoundException(PersonNotFoundException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.info(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

}
