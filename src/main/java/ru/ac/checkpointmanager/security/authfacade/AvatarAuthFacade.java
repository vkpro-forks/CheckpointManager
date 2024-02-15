package ru.ac.checkpointmanager.security.authfacade;

import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.service.avatar.AvatarService;

import java.util.UUID;
@Component("avatarAuthFacade")
public final class AvatarAuthFacade implements AuthFacade {

    private final AvatarService avatarService;

    private AvatarAuthFacade(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @Override
    public boolean isIdMatch(UUID avatarId) {
        User user = getCurrentUser();
        Avatar avatar = avatarService.findAvatarById(avatarId);
        return avatar.getUser().getId().equals(user.getId());
    }
}
