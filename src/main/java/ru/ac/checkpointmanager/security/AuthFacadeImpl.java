package ru.ac.checkpointmanager.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.User;

import java.util.UUID;

/**
 * Класс, предоставляющий методы для работы с контекстом безопасности Spring Security.
 * <p>
 * Класс содержит статические методы для получения информации о текущем аутентифицированном
 * пользователе, такие как получение самого пользователя или его уникального идентификатора (UUID).
 * Методы класса могут выбрасывать {@link AccessDeniedException}, если пользователь не аутентифицирован.
 * <p>
 *
 * @see CustomAuthenticationToken
 * @see User
 */
@Component("authFacade")
public class AuthFacadeImpl implements AuthFacade {

    @Override
    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Получает текущего аутентифицированного пользователя из контекста безопасности.
     *
     * @return текущий аутентифицированный пользователь.
     * @throws AccessDeniedException если пользователь не аутентифицирован.
     * @see CustomAuthenticationToken
     */
    @Override
    public User getCurrentUser() {
        Authentication authentication = getAuthentication();
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
    @Override
    public UUID getUserUUID() {
        Authentication authentication = getAuthentication();
        if (!(authentication instanceof CustomAuthenticationToken)) {
            throw new AccessDeniedException("User is not authenticated");
        }
        return ((CustomAuthenticationToken) authentication).getUserId();
    }

    @Override
    public boolean isUserIdMatch(UUID userId) {
        Authentication authentication = getAuthentication();
        if (!(authentication instanceof CustomAuthenticationToken customToken)) {
            throw new AccessDeniedException("User is not authenticated");
        }
        UUID currentUserUUID = customToken.getUserId();
        return currentUserUUID.equals(userId);
    }
}
