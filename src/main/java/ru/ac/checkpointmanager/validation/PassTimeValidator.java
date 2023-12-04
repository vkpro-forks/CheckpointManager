package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.validation.annotation.PassTimeCheck;

/**
 * Класс проверяет, что в пропуске дата начала < дата окончания
 */
@Slf4j
public class PassTimeValidator implements ConstraintValidator<PassTimeCheck, Object> {

    private String validationMessage;

    @Override
    public void initialize(PassTimeCheck constraintAnnotation) {
        validationMessage = constraintAnnotation.message();
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
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
        if (value instanceof PassUpdateDTO passUpdateDTO) {
            return passUpdateDTO.getStartTime().isBefore(passUpdateDTO.getEndTime());
        }
        if (value instanceof PassCreateDTO passCreateDTO) {
            return passCreateDTO.getStartTime().isBefore(passCreateDTO.getEndTime());
        }
        log.debug("Pass time validation successful");
        return false;
    }
}
