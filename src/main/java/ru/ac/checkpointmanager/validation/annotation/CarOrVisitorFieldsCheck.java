package ru.ac.checkpointmanager.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.ac.checkpointmanager.validation.CarOrVisitorFieldsValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CarOrVisitorFieldsValidator.class)
@Documented
public @interface CarOrVisitorFieldsCheck {

    String message() default "Dto should contains Car or Visitor field";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
