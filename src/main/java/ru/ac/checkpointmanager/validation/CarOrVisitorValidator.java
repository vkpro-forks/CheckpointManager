package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.ac.checkpointmanager.dto.passes.PassDtoCreate;
import ru.ac.checkpointmanager.dto.passes.PassDtoUpdate;
import ru.ac.checkpointmanager.validation.annotation.CarOrVisitor;

public class CarOrVisitorValidator implements ConstraintValidator<CarOrVisitor, Object> {

    public static final String VALIDATION_MSG = "%s is null";
    public static final String CAR = "car";
    public static final String VISITOR = "visitor";

    @Override
    public void initialize(CarOrVisitor constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(VALIDATION_MSG.formatted(CAR))
                .addPropertyNode(CAR)
                .addConstraintViolation()
                .buildConstraintViolationWithTemplate(VALIDATION_MSG.formatted(VISITOR))
                .addPropertyNode(VISITOR)
                .addConstraintViolation();
        if (value == null) {
            return true;//not responsibility of this annotation
        }
        if (value instanceof PassDtoUpdate passDtoUpdate) {
            return passDtoUpdate.getVisitor() != null && passDtoUpdate.getCar() != null;
        }
        if (value instanceof PassDtoCreate passDtoCreate) {
            return passDtoCreate.getVisitor() != null && passDtoCreate.getCar() != null;
        }
        return false;
    }

}
