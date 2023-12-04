package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;
import ru.ac.checkpointmanager.validation.annotation.AvatarImageCheck;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class AvatarImageValidator implements ConstraintValidator<AvatarImageCheck, MultipartFile> {

    private static final String NOT_SUPPORTED_OF_AVATAR_FILE = "Not supported %s of avatar file";

    private static final String VALIDATION_FAILED = "Validation failed: ";

    private final AvatarProperties avatarProperties;

    @Override
    public void initialize(AvatarImageCheck constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        String validationMessage;
        log.debug("Validating avatar file...");
        if (value == null || value.isEmpty()) {
            validationMessage = "The avatar file is empty or null";
            log.warn(VALIDATION_FAILED + validationMessage);
            configureErrorMessage(context, validationMessage);
            return false;
        }
        log.debug("Checking file extension...");
        String filename = value.getResource().getFilename();
        Optional<String> optionalExtension = Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase());
        if (optionalExtension.isEmpty()) {
            validationMessage = "No extension in avatar file";
            log.warn(VALIDATION_FAILED + validationMessage);
            configureErrorMessage(context, validationMessage);
            return false;
        }
        if (!avatarProperties.getExtensions().contains(optionalExtension.get())) {
            validationMessage = NOT_SUPPORTED_OF_AVATAR_FILE.formatted("extension");
            log.warn(VALIDATION_FAILED + validationMessage);
            configureErrorMessage(context, validationMessage);
            return false;
        }
        log.debug("Checking file content type...");
        String contentType = value.getContentType();
        if (contentType == null ||
                !contentType.startsWith(avatarProperties.getContentType())) {
            validationMessage = NOT_SUPPORTED_OF_AVATAR_FILE.formatted("contentType");
            log.warn(VALIDATION_FAILED + validationMessage);
            configureErrorMessage(context, validationMessage);
            return false;
        }
        log.debug("Avatar file validation successful");
        return true;
    }

    private void configureErrorMessage(ConstraintValidatorContext context, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }

}
