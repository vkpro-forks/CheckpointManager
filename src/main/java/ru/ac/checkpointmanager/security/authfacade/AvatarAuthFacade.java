package ru.ac.checkpointmanager.security.authfacade;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.model.User;
import ru.ac.checkpointmanager.model.avatar.Avatar;
import ru.ac.checkpointmanager.service.avatar.AvatarService;

import java.util.UUID;
@Component("avatarAuthFacade")
@AllArgsConstructor
@Slf4j
public class AvatarAuthFacade implements AuthFacade {

    private AvatarService avatarService;

    @Override
    public boolean isIdMatch(UUID avatarId) {
        User user = getCurrentUser();
        Avatar avatar = avatarService.findAvatarById(avatarId);
        return avatar.getUser().getId().equals(user.getId());
    }
}
