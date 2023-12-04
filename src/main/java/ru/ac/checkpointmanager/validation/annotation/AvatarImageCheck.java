package ru.ac.checkpointmanager.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.ac.checkpointmanager.validation.AvatarImageValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AvatarImageValidator.class)
@Documented
public @interface AvatarImageCheck {

    String message() default "Avatar image validation failed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
