package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import ru.ac.checkpointmanager.dto.passes.PassBaseDTO;
import ru.ac.checkpointmanager.validation.annotation.PassTimeCheck;

/**
 * Класс проверяет, что в пропуске дата начала < дата окончания
 */
@Slf4j
public class PassTimeValidator implements ConstraintValidator<PassTimeCheck, PassBaseDTO> {

    private String validationMessage;

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
        boolean isValid = value.getStartTime().isBefore(value.getEndTime());
        if (!isValid) {
            log.debug("Pass time validation failed");
            return false;
        }
        log.debug("Pass time validation successful");
        return true;
    }
}
