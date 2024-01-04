package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.ac.checkpointmanager.model.User;

import java.util.UUID;

/**
 * Интерфейс AuthFacade предоставляет методы для работы с аутентификацией пользователя.
 */
public interface AuthFacade {

    /**
     * Получает текущего аутентифицированного пользователя из контекста безопасности.
     * Этот метод использует {@link SecurityContextHolder} для извлечения данных аутентификации.
     *
     * @return текущий аутентифицированный пользователь как объект {@link User}.
     * @throws AccessDeniedException если пользователь не аутентифицирован.
     *         Это исключение генерируется, если {@link Authentication} объект не найден в контексте безопасности.
     */
    default User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AccessDeniedException("User is not authenticated");
        }

        return (User) authentication.getPrincipal();
    }

    /**
     * Проверяет на совпадение переданный UUID с идентификатором в контексте текущей реализации.
     * Этот метод может быть реализован для сравнения UUID различных сущностей.
     *
     * @param id UUID, который необходимо проверить на совпадение.
     * @return true, если переданный UUID соответствует ожидаемому в данной реализации, иначе false.
     */
    boolean isIdMatch(UUID id);
}
