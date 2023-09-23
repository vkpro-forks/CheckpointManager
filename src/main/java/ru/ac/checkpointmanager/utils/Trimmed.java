package ru.ac.checkpointmanager.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TrimmedValidator.class)
public @interface Trimmed {

    String message() default "Value must not contain leading or trailing spaces";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}