package ru.ac.checkpointmanager.security.authfacade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.User;

import java.util.UUID;

@Component("userAuthFacade")
@RequiredArgsConstructor
@Slf4j
public class UserAuthFacade implements AuthFacade {

    @Override
    public boolean isIdMatch(UUID userId) {
        User user = getCurrentUser();
        UUID currentUserUUID = user.getId();
        return currentUserUUID.equals(userId);
    }
}
