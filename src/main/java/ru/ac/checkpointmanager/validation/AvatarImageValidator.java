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

    private static final String NOT_SUPPORTED_OF_AVATAR_FILE = "Not supported {} of avatar file";

    private static final String VALIDATION_FAILED = "Validation failed: ";

    private final AvatarProperties avatarProperties;

    @Override
    public void initialize(AvatarImageCheck constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(VALIDATION_FAILED + "avatar file doesn't meet requirements")
                .addPropertyNode("avatar")
                .addConstraintViolation();
        log.debug("Validating avatar file...");
        if (value == null || value.isEmpty()) {
            log.warn(VALIDATION_FAILED + "The avatar file is empty or null");
            return false;
        }
        log.debug("Checking file extension...");
        String filename = value.getResource().getFilename();
        Optional<String> optionalExtension = Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase());
        if (optionalExtension.isEmpty()) {
            log.warn(VALIDATION_FAILED + "No extension in avatar file");
            return false;
        }
        if (!avatarProperties.getExtensions().contains(optionalExtension.get())) {
            log.warn(VALIDATION_FAILED + NOT_SUPPORTED_OF_AVATAR_FILE, "extension");
            return false;
        }
        log.debug("Checking file content type...");
        String contentType = value.getContentType();
        if (contentType == null ||
                !contentType.startsWith(avatarProperties.getContentType())) {
            log.warn(VALIDATION_FAILED + NOT_SUPPORTED_OF_AVATAR_FILE, "contentType");
            return false;
        }
        log.info("Avatar file validation successful");
        return true;
    }

}
