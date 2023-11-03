package ru.ac.checkpointmanager.utils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.ac.checkpointmanager.exception.*;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CarBrandNotFoundException.class)
    public ResponseEntity<String> handleCarBrandNotFoundException(CarBrandNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.I_AM_A_TEAPOT);
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
        log.warn("Handling EntityNotFoundException: " + ex.getMessage());
        return new ResponseEntity<>(new ApiError(HttpStatus.I_AM_A_TEAPOT, ex.getMessage(),
                (List<String>) null), HttpStatus.I_AM_A_TEAPOT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex) {
        log.warn("Handling Exception: " + ex.getMessage());
        return new ResponseEntity<>(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(),
                (List<String>) null), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntranceWasAlreadyException.class)
    public ResponseEntity<String> handleEntranceWasAlreadyException(EntranceWasAlreadyException e) {
        log.warn("Handling EntranceWasAlreadyException: " + e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InactivePassException.class)
    public ResponseEntity<String> handleNoActivePassException(InactivePassException e) {
        log.warn("Handling NoActivePassException: " + e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TerritoryNotFoundException.class)
    public ResponseEntity<String> handleTerritoryNotFoundException(TerritoryNotFoundException e) {
        log.warn("Handling TerritoryNotFoundException: " + e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.I_AM_A_TEAPOT);
    }

    @ExceptionHandler(PassNotFoundException.class)
    public ResponseEntity<String> handlePassNotFoundException(PassNotFoundException e) {
        log.info("Handling PassNotFoundException: " + e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.I_AM_A_TEAPOT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleAvatarIsTooBigException(MaxUploadSizeExceededException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(new ApiError(HttpStatus.BAD_REQUEST, message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AvatarNotFoundException.class)
    public ResponseEntity<ApiError> handleAvatarNotFoundException(AvatarNotFoundException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(new ApiError(HttpStatus.I_AM_A_TEAPOT, message), HttpStatus.I_AM_A_TEAPOT);
    }

    @ExceptionHandler(AvatarIsEmptyException.class)
    public ResponseEntity<ApiError> handleAvatarIsEmptyException(AvatarIsEmptyException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(new ApiError(HttpStatus.BAD_REQUEST, message), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadAvatarExtensionException.class)
    public ResponseEntity<ApiError> handleBadAvatarExtensionException(BadAvatarExtensionException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(new ApiError(HttpStatus.BAD_REQUEST, message), HttpStatus.BAD_REQUEST);
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
        return new ResponseEntity<>(message, HttpStatus.I_AM_A_TEAPOT);
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
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(PhoneAlreadyExistException.class)
    public ResponseEntity<String> handlePhoneAlreadyExistException(PhoneAlreadyExistException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.CONFLICT);
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


    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<String> handleMailSendException(MailSendException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException e) {
        String message = String.format("Exception %s: %s", e.getClass(), e.getMessage());
        log.warn(message);
        return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }
}
