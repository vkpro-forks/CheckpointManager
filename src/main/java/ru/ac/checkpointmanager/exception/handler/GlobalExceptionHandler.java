package ru.ac.checkpointmanager.exception.handler;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.ac.checkpointmanager.exception.DateOfBirthFormatException;
import ru.ac.checkpointmanager.exception.EntranceWasAlreadyException;
import ru.ac.checkpointmanager.exception.InvalidPhoneNumberException;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.exception.ObjectAlreadyExistsException;
import ru.ac.checkpointmanager.exception.PhoneAlreadyExistException;
import ru.ac.checkpointmanager.exception.VisitorNotFoundException;
import ru.ac.checkpointmanager.exception.pass.InactivePassException;
import ru.ac.checkpointmanager.exception.pass.PassException;

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
                        formatValidationCurrentValue(v.getInvalidValue())))
                .toList();
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        ProblemDetail configuredProblemDetails = setUpValidationDetails(problemDetail, violationErrors);
        log.debug(LOG_MSG_DETAILS, e.getClass(), e.getMessage());
        return configuredProblemDetails;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        final List<ViolationError> violationErrors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> new ViolationError(
                        error.getField(), error.getDefaultMessage(),
                        formatValidationCurrentValue(error.getRejectedValue())))
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

    @ExceptionHandler(PassException.class)
    public ProblemDetail handlePassException(PassException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Pass creating error");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(ObjectAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExistsException(ObjectAlreadyExistsException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, e);
        problemDetail.setTitle("Object already exists");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.CONFLICT.toString());
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

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.FORBIDDEN, e);
        problemDetail.setTitle("Access denied");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.FORBIDDEN.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }


    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthenticationException(AuthenticationException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.UNAUTHORIZED, e);
        problemDetail.setTitle("Authentication error");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.UNAUTHORIZED.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail handleExpiredJwtException(ExpiredJwtException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.FORBIDDEN, e);
        problemDetail.setTitle("Jwt expired, send refresh token");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.TOKEN_EXPIRED.toString());
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

    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidTokenException(InvalidTokenException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.FORBIDDEN, e);
        problemDetail.setTitle("Jwt is invalid");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.FORBIDDEN.toString());
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

    private String formatValidationCurrentValue(Object object) {
        if (object == null) {
            return "null";
        }
        if (object.toString().contains(object.getClass().getName())) {
            return object.getClass().getSimpleName();
        }
        return object.toString();
    }

}