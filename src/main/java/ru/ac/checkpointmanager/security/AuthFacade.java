package ru.ac.checkpointmanager.security;

import org.springframework.security.core.Authentication;
import ru.ac.checkpointmanager.model.User;

import java.util.UUID;

public interface AuthFacade {
    Authentication getAuthentication();

    User getCurrentUser();

    UUID getUserUUID();

    boolean isUserIdMatch(UUID userId);

    boolean isPhoneIdMatch(UUID phoneId);
}
