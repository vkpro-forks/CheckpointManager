package ru.ac.checkpointmanager.security;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        if (authentication == null) {
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
        if (authentication == null) {
            throw new AccessDeniedException("User is not authenticated");
        }
        return ((CustomAuthenticationToken) authentication).getUserId();
    }

    @Override
    public boolean isUserIdMatch(UUID userId) {
        log.debug("ID {} is being checked", userId);
        Authentication authentication = getAuthentication();

        if (authentication == null) {
            throw new AccessDeniedException("User is not authenticated");
        }

        CustomAuthenticationToken customToken = (CustomAuthenticationToken) authentication;
        UUID currentUserUUID = customToken.getUserId();
        log.debug("User ID from token {}", currentUserUUID.toString());
        return currentUserUUID.equals(userId);
    }

}
