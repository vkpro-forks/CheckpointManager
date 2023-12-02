package ru.ac.checkpointmanager.exception.handler;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.ac.checkpointmanager.exception.*;

import java.time.Instant;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String ERROR_CODE = "errorCode";

    private static final String TIMESTAMP = "timestamp";

    private static final String LOG_MSG = "[Exception {}] handled";

    private static final String LOG_MSG_DETAILS = "[Exception {} with message {}] handled";

    public static final String VALIDATION_ERROR = "Validation error";

    public static final String VIOLATIONS = "violations";

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.NOT_FOUND, e);
        problemDetail.setTitle("Object not found");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.NOT_FOUND.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolationException(ConstraintViolationException e) {
        List<ViolationError> violationErrors = e.getConstraintViolations().stream()
                .map(v -> new ViolationError(
                        fieldNameFromPath(v.getPropertyPath().toString()),
                        v.getMessage(),
                        v.getInvalidValue() != null ? v.getInvalidValue().toString() : "null"))
                .toList();
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        ProblemDetail configuredProblemDetails = setUpValidationDetails(problemDetail, violationErrors);
        log.debug(LOG_MSG_DETAILS, e.getClass(), e.getMessage());
        return configuredProblemDetails;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        final List<ViolationError> violationErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ViolationError(error.getField(), error.getDefaultMessage(),
                        error.getRejectedValue() != null ? error.getRejectedValue().toString() : "null"))
                .toList();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problemDetail.setProperty(TIMESTAMP, Instant.now());
        ProblemDetail configuredProblemDetails = setUpValidationDetails(problemDetail, violationErrors);
        log.debug(LOG_MSG_DETAILS, e.getClass(), e.getMessage());
        return configuredProblemDetails;
    }

    @ExceptionHandler(EntranceWasAlreadyException.class)
    public ProblemDetail handleEntranceWasAlreadyException(EntranceWasAlreadyException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, e);
        problemDetail.setTitle("Entrance was already");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.CONFLICT.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(InactivePassException.class)
    public ProblemDetail handleNoActivePassException(InactivePassException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Inactive pass");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Illegal argument exception occurred");//FIXME replace for more suitable exception
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleAvatarIsTooBigException(MaxUploadSizeExceededException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Size of uploading file exceeds maximum");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(AvatarIsEmptyException.class)
    public ProblemDetail handleAvatarIsEmptyException(AvatarIsEmptyException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Avatar is empty");// FIXME No usages for this exception
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(BadAvatarExtensionException.class)
    public ProblemDetail handleBadAvatarExtensionException(BadAvatarExtensionException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Not suitable extension for avatar");//FIXME No usages for this exception
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.FORBIDDEN, e);
        problemDetail.setTitle("Access denied");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.FORBIDDEN.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(DateOfBirthFormatException.class)
    public ProblemDetail handleDateOfBirthFormatException(DateOfBirthFormatException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Bad format of birth date"); //FIXME no usages, may be would be better to have VALIDATION
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Illegal state exception occurred");//FIXME need new more suitable exception
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(PhoneAlreadyExistException.class)
    public ProblemDetail handlePhoneAlreadyExistException(PhoneAlreadyExistException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, e);
        problemDetail.setTitle("Phone number already exists");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.CONFLICT.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleUsernameNotFoundException(UsernameNotFoundException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.NOT_FOUND, e);
        problemDetail.setTitle("Username not found");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.NOT_FOUND.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(VisitorNotFoundException.class)
    public ProblemDetail handleVisitorNotFoundException(VisitorNotFoundException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.NOT_FOUND, e);
        problemDetail.setTitle("Visitor not found");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.NOT_FOUND.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }


    @ExceptionHandler(MailSendException.class)
    public ProblemDetail handleMailSendException(MailSendException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, e);
        problemDetail.setTitle("Error during send mail");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.INTERNAL_SERVER_ERROR.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Bad credentials");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(InvalidPhoneNumberException.class)
    public ProblemDetail handleInvalidPhoneNumberException(InvalidPhoneNumberException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Invalid phone number format"); //FIXME move to validation
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    private ProblemDetail createProblemDetail(HttpStatus status, Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, e.getMessage());
        problemDetail.setProperty(TIMESTAMP, Instant.now());
        return problemDetail;
    }

    private ProblemDetail setUpValidationDetails(ProblemDetail problemDetail, List<ViolationError> violationErrors) {
        problemDetail.setTitle(VALIDATION_ERROR);
        problemDetail.setProperty(ERROR_CODE, ErrorCode.VALIDATION.toString());
        problemDetail.setProperty(VIOLATIONS, violationErrors);
        return problemDetail;
    }

    private String fieldNameFromPath(String path) {
        String[] split = path.split("\\.");
        if (split.length > 1) {
            return split[split.length - 1];
        }
        return path;
    }

}