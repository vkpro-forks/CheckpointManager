package ru.ac.checkpointmanager.annotation;

import ru.ac.checkpointmanager.security.authfacade.PassAuthFacade;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to check rights of the user who tries to perform some actions with passes
 *
 * @see PassAuthFacade
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ROLE_ADMIN') or @passAuthFacade.isIdMatch(#passId)")
public @interface PreAuthorizeAdminOrPassOwner {
}
