package ru.ac.checkpointmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import ru.ac.checkpointmanager.dto.passes.PassCreateDTO;
import ru.ac.checkpointmanager.dto.passes.PassUpdateDTO;
import ru.ac.checkpointmanager.validation.annotation.CarOrVisitorFieldsCheck;

/**
 * Класс проверяет содержит ли Объект поля Car и Visitor: в логике допустимо только одно поле
 * ЛИБО Car, ЛИБО Visitor;
 * Если передаются оба поля - ошибка валидации
 * Если оба поля null - ошибка валидации
 */
@Slf4j
public class CarOrVisitorFieldsValidator implements ConstraintValidator<CarOrVisitorFieldsCheck, Object> {

    public static final String VALIDATION_MSG = "%s field is null, or present with %s;" +
            " should be only Car or Visitor field";
    public static final String CAR = "car";
    public static final String VISITOR = "visitor";

    @Override
    public void initialize(CarOrVisitorFieldsCheck constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        log.debug("Validating Car or Visitor in Pass...");
        context.buildConstraintViolationWithTemplate(VALIDATION_MSG.formatted(CAR, VISITOR))
                .addPropertyNode(CAR)
                .addConstraintViolation()
                .buildConstraintViolationWithTemplate(VALIDATION_MSG.formatted(VISITOR, CAR))
                .addPropertyNode(VISITOR)
                .addConstraintViolation();
        if (value == null) {
            return true;//not responsibility of this annotation
        }
        //true if only one of parameters is null (null+ null = false, present+present = false)
        if (value instanceof PassUpdateDTO passUpdateDTO) {
            return passUpdateDTO.getVisitor() != null ^ passUpdateDTO.getCar() != null;
        }
        if (value instanceof PassCreateDTO passCreateDTO) {
            return passCreateDTO.getVisitor() != null ^ passCreateDTO.getCar() != null;
        }
        log.debug("Pass has only car, or only visitor: validation successful");
        return false;
    }

}
