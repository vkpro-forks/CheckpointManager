package ru.ac.checkpointmanager.utils;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.ac.checkpointmanager.configuration.CustomAuthenticationToken;
import ru.ac.checkpointmanager.model.User;

import java.util.UUID;

/**
 * Утилитный класс, предоставляющий методы для работы с контекстом безопасности Spring Security.
 * <p>
 * Класс содержит статические методы для получения информации о текущем аутентифицированном
 * пользователе, такие как получение самого пользователя или его уникального идентификатора (UUID).
 * Методы класса могут выбрасывать {@link AccessDeniedException}, если пользователь не аутентифицирован.
 * <p>
 * @see CustomAuthenticationToken
 * @see User
 */
public class SecurityUtils {

    /**
     * Получает текущего аутентифицированного пользователя из контекста безопасности.
     *
     * @return текущий аутентифицированный пользователь.
     * @throws AccessDeniedException если пользователь не аутентифицирован.
     * @see CustomAuthenticationToken
     */
    public static User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof CustomAuthenticationToken)) {
            throw new AccessDeniedException("User is not authenticated");
        }

        return (User) authentication.getPrincipal();
    }

    /**
     * Извлекает ID текущего аутентифицированного пользователя из контекста безопасности.
     *
     * @return UUID.
     * @throws AccessDeniedException если пользователь не аутентифицирован.
     * @see CustomAuthenticationToken
     */
    public static UUID getUserUUID() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof CustomAuthenticationToken)) {
            throw new AccessDeniedException("User is not authenticated");
        }
        UUID userId = ((CustomAuthenticationToken) authentication).getUserId();
        return userId;
    }
}