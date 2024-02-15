package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.User;

import java.util.UUID;

@Component("userAuthFacade")
public final class UserAuthFacade implements AuthFacade {

    private UserAuthFacade() {
    }

    @Override
    public boolean isIdMatch(UUID userId) {
        User user = getCurrentUser();
        UUID currentUserUUID = user.getId();
        return currentUserUUID.equals(userId);
    }
}
