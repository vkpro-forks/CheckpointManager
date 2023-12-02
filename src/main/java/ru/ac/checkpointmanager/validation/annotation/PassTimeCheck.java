package ru.ac.checkpointmanager.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.ac.checkpointmanager.validation.PassTimeValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PassTimeValidator.class)
@Documented
public @interface PassTimeCheck {

    String message() default "Start time should be before end time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
