package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.ac.checkpointmanager.dto.passes.PassDtoCreate;
import ru.ac.checkpointmanager.dto.passes.PassDtoUpdate;
import ru.ac.checkpointmanager.validation.annotation.PassTimeCheck;

public class PassTimeValidator implements ConstraintValidator<PassTimeCheck, Object> {

    public static final String VALIDATION_MSG = "The start time is after the end time";

    @Override
    public void initialize(PassTimeCheck constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        context.buildConstraintViolationWithTemplate(VALIDATION_MSG)
                .addPropertyNode("startTime")
                .addConstraintViolation()
                .buildConstraintViolationWithTemplate(VALIDATION_MSG)
                .addPropertyNode("endTime")
                .addConstraintViolation();
        if (value == null) {
            return true;//not responsibility of this annotation
        }
        if (value instanceof PassDtoUpdate passDtoUpdate) {
            return passDtoUpdate.getStartTime().isBefore(passDtoUpdate.getEndTime());
        }
        if (value instanceof PassDtoCreate passDtoCreate) {
            return passDtoCreate.getStartTime().isBefore(passDtoCreate.getEndTime());
        }
        return false;
    }

}
