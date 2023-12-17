package ru.ac.checkpointmanager.security;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

/**
 * Расширенная реализация {@link UsernamePasswordAuthenticationToken},
 * содержащая дополнительный идентификатор пользователя (userId).
 * Используется для хранения и доступа к идентификатору пользователя в контексте безопасности.
 */
@Getter
public class CustomAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final UUID userId;

    /**
     * Конструктор для создания {@link CustomAuthenticationToken}.
     *
     * @param principal     Основная информация о пользователе (объект {@link ru.ac.checkpointmanager.model.User}).
     * @param credentials   Учетные данные пользователя, в данном контексте обычно null,
     *                      так как аутентификация осуществляется через JWT.
     * @param userId        Уникальный идентификатор пользователя, связанный с токеном.
     * @param authorities   Список ролей или полномочий, предоставленных пользователю.
     */
    public CustomAuthenticationToken(Object principal,
                                     Object credentials,
                                     UUID userId,
                                     Collection<? extends GrantedAuthority> authorities
    ) {
        super(principal, credentials, authorities);
        this.userId = userId;
    }
}
