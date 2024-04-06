package ru.ac.checkpointmanager.exception.handler;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ru.ac.checkpointmanager.exception.CriticalServerException;
import ru.ac.checkpointmanager.exception.EmailVerificationTokenException;
import ru.ac.checkpointmanager.exception.ImageProcessingException;
import ru.ac.checkpointmanager.exception.InvalidTokenException;
import ru.ac.checkpointmanager.exception.MismatchCurrentPasswordException;
import ru.ac.checkpointmanager.exception.ObjectAlreadyExistsException;
import ru.ac.checkpointmanager.exception.ObjectsRelationConflictException;
import ru.ac.checkpointmanager.exception.PassAlreadyUsedException;
import ru.ac.checkpointmanager.exception.PasswordConfirmationException;
import ru.ac.checkpointmanager.exception.PhoneAlreadyExistException;
import ru.ac.checkpointmanager.exception.VisitorNotFoundException;
import ru.ac.checkpointmanager.exception.pass.PassException;
import ru.ac.checkpointmanager.exception.payment.DonationException;

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
        problemDetail.setTitle(ErrorMessage.OBJECT_NOT_FOUND);
        problemDetail.setProperty(ERROR_CODE, ErrorCode.NOT_FOUND.toString());
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

    @ExceptionHandler(PassAlreadyUsedException.class)
    public ProblemDetail handleEntranceWasAlreadyException(PassAlreadyUsedException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, e);
        problemDetail.setTitle("Entrance was already");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.CONFLICT.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(PassException.class)
    public ProblemDetail handlePassException(PassException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle(ErrorMessage.PASS_EXCEPTION_TITLE);
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(ObjectAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExistsException(ObjectAlreadyExistsException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, e);
        problemDetail.setTitle(ErrorMessage.OBJECT_ALREADY_EXISTS);
        problemDetail.setProperty(ERROR_CODE, ErrorCode.CONFLICT.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(ObjectsRelationConflictException.class)
    public ProblemDetail handleAlreadyExistsException(ObjectsRelationConflictException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, e);
        problemDetail.setTitle("Objects relation already exists or has conflict");
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

    @ExceptionHandler(EmailVerificationTokenException.class)
    public ProblemDetail handleEmailVerificationTokenException(EmailVerificationTokenException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Verification token not found, or expired");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(ImageProcessingException.class)
    public ProblemDetail handleImageProcessingException(ImageProcessingException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Image processing exception");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(MismatchCurrentPasswordException.class)
    public ProblemDetail handlePhoneAlreadyExistException(MismatchCurrentPasswordException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.CONFLICT, e);
        problemDetail.setTitle("Current password not matched");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.CONFLICT.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(PasswordConfirmationException.class)
    public ProblemDetail handleEmailVerificationTokenException(PasswordConfirmationException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle("Passwords are not the same");
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle(ErrorMessage.WRONG_ARGUMENT_PASSED);
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMethodArgumentTypeMismatchException(MissingServletRequestParameterException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle(ErrorMessage.MISSING_REQUEST_PARAM);
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        log.debug(LOG_MSG, e.getClass());
        return problemDetail;
    }

    @ExceptionHandler(DonationException.class)
    public ProblemDetail handleDonationException(DonationException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, e);
        problemDetail.setTitle(ErrorMessage.DONATION_ERROR);
        problemDetail.setProperty(ERROR_CODE, ErrorCode.INTERNAL_SERVER_ERROR.toString());
        return problemDetail;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.BAD_REQUEST, e);
        problemDetail.setTitle(ErrorMessage.WRONG_ARGUMENT_PASSED);
        problemDetail.setProperty(ERROR_CODE, ErrorCode.BAD_REQUEST.toString());
        return problemDetail;
    }

    // 500
    @ExceptionHandler(CriticalServerException.class)
    public ProblemDetail handleCriticalServerException(CriticalServerException e) {
        ProblemDetail problemDetail = createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, e);
        problemDetail.setTitle(ErrorMessage.INTERNAL_SERVER_ERROR);
        problemDetail.setProperty(ERROR_CODE, ErrorCode.INTERNAL_SERVER_ERROR.toString());
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