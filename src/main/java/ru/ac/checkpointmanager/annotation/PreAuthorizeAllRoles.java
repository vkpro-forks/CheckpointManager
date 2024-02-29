package ru.ac.checkpointmanager.annotation;

import org.springframework.security.access.prepost.PreAuthorize;
import ru.ac.checkpointmanager.model.enums.Role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для проверки, обладает ли пользователь ролью, указанной в списке: <p>
 * - {@link Role#ADMIN}<p>
 * - {@link Role#MANAGER}<p>
 * - {@link Role#SECURITY}<p>
 * - {@link Role#USER}<p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_SECURITY', 'ROLE_USER')")
public @interface PreAuthorizeAllRoles {
}
