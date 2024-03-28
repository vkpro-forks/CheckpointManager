package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import ru.ac.checkpointmanager.dto.passes.PassBaseDTO;
import ru.ac.checkpointmanager.validation.annotation.PassTimeCheck;

import java.time.Duration;

/**
 * Класс проверяет, что в пропуске дата начала < дата окончания
 * и что время действия пропуска не превышает значение {@code  VALIDITY_PERIOD_OF_THE_PASS}
 */
@Slf4j
public class PassTimeValidator implements ConstraintValidator<PassTimeCheck, PassBaseDTO> {

    private String validationMessage;
    private static final long VALIDITY_PERIOD_OF_THE_PASS = 30;

    @Override
    public void initialize(PassTimeCheck constraintAnnotation) {
        validationMessage = constraintAnnotation.message();
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(PassBaseDTO value, ConstraintValidatorContext context) {
        log.debug("Validating pass time...");
        context.buildConstraintViolationWithTemplate(validationMessage)
                .addPropertyNode("startTime")
                .addConstraintViolation()
                .buildConstraintViolationWithTemplate(validationMessage)
                .addPropertyNode("endTime")
                .addConstraintViolation();
        if (value == null) {
            return true;//not responsibility of this annotation
        }
        if (!endTimeExceedsLimit(value)) {
            log.debug("End time validation failed");
            return false;
        }
        boolean isValid = value.getStartTime().isBefore(value.getEndTime());
        if (!isValid) {
            log.debug("Pass time validation failed");
            return false;
        }
        log.debug("Pass time validation successful");
        return true;
    }

    private boolean endTimeExceedsLimit(PassBaseDTO value) {
        return (Duration.between(value.getStartTime(), value.getEndTime())).toDays() <= VALIDITY_PERIOD_OF_THE_PASS;
    }
}
