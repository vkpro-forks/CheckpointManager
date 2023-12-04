package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import ru.ac.checkpointmanager.model.avatar.AvatarProperties;
import ru.ac.checkpointmanager.validation.annotation.AvatarImageCheck;

import java.util.Optional;

/**
 * Класс проверяет, что в переданный файл для аватара:
 * - не пустой;
 * - имеет правильное расширение (которые мы указали в applucation.yaml)
 * - имеет правильный тип контента
 */
@Slf4j
@RequiredArgsConstructor
public class AvatarImageValidator implements ConstraintValidator<AvatarImageCheck, MultipartFile> {

    private static final String NOT_SUPPORTED_OF_AVATAR_FILE = ": not supported %s of avatar file";

    private String validationMessage;

    private final AvatarProperties avatarProperties;

    @Override
    public void initialize(AvatarImageCheck constraintAnnotation) {
        validationMessage = constraintAnnotation.message();
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        String detailedMessage;
        log.debug("Validating avatar file...");
        if (value.isEmpty()) {
            detailedMessage = ": the avatar file is empty";
            log.warn(validationMessage + detailedMessage);
            configureErrorMessage(context, detailedMessage);
            return false;
        }
        log.debug("Checking file extension...");
        String filename = value.getResource().getFilename();
        Optional<String> optionalExtension = Optional.ofNullable(filename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(filename.lastIndexOf(".") + 1).toLowerCase());
        if (optionalExtension.isEmpty()) {
            detailedMessage = ": no extension in avatar file";
            log.warn(validationMessage + detailedMessage);
            configureErrorMessage(context, detailedMessage);
            return false;
        }
        if (!avatarProperties.getExtensions().contains(optionalExtension.get())) {
            detailedMessage = NOT_SUPPORTED_OF_AVATAR_FILE.formatted("extension");
            log.warn(validationMessage + detailedMessage);
            configureErrorMessage(context, detailedMessage);
            return false;
        }
        log.debug("Checking file content type...");
        String contentType = value.getContentType();
        if (contentType == null ||
                !contentType.startsWith(avatarProperties.getContentType())) {
            detailedMessage = NOT_SUPPORTED_OF_AVATAR_FILE.formatted("contentType");
            log.warn(validationMessage + detailedMessage);
            configureErrorMessage(context, detailedMessage);
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
