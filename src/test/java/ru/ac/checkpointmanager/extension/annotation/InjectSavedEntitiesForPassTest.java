package ru.ac.checkpointmanager.extension.annotation;

import org.junit.jupiter.api.extension.ExtendWith;
import ru.ac.checkpointmanager.extension.PassTestingPreparationExtension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пометки объекта, в который будут инжектиться данные из extension
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(PassTestingPreparationExtension.class)
public @interface InjectSavedEntitiesForPassTest {
}
