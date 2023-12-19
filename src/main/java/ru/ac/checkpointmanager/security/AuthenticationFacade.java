package ru.ac.checkpointmanager.security;

import org.springframework.security.core.Authentication;
import ru.ac.checkpointmanager.model.User;

import java.util.UUID;

public interface AuthenticationFacade {
    Authentication getAuthentication();

    User getCurrentUser();

    UUID getUserUUID();
}
