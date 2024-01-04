package ru.ac.checkpointmanager.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.enums.Role;
import ru.ac.checkpointmanager.security.CustomAuthenticationToken;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Фабрика, которая создает мок Секьюрити контекста с установкой туда нашего кастомного юзера (с айди)
 */
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User user = new User();
        user.setEmail(customUser.username());
        user.setId(UUID.fromString(customUser.id()));
        user.setRole(Role.valueOf(customUser.role()));
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + customUser.role()));
        CustomAuthenticationToken authToken = new CustomAuthenticationToken(user, null, user.getId(), authorities);
        context.setAuthentication(authToken);
        return context;
    }

}
