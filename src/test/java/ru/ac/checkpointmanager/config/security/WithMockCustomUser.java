package ru.ac.checkpointmanager.config.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Кастомная аннотация для подстановки в Секьюрити контекст юзера с кастомными полями;
 * По умолчанию устанавливается UsernamePasswordToken, но там нет ID
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

    String username() default "Vasya";

    String id() default "750cee36-b9dc-4534-9872-0c167cdc73c5"; //можно передать только явно константой

    String role() default "ADMIN";

}
