package ru.ac.checkpointmanager.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.service.phone.PhoneService;

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
@RequiredArgsConstructor
@Slf4j
public class AuthFacadeImpl implements AuthFacade {

    private final PhoneService phoneService;

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

        UUID currentUserUUID = getUserUUID();
        return currentUserUUID.equals(userId);
    }

    @Override
    public boolean isPhoneIdMatch(UUID phoneId) {
        User user = getCurrentUser();
        Phone phone = phoneService.findPhoneById(phoneId);
        return phone.getUser().equals(user);
    }
}
