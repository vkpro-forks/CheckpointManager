package ru.ac.checkpointmanager.mapper.avatar;

import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.model.avatar.Avatar;

import java.util.Collection;
import java.util.List;

public interface AvatarMapper {

    Avatar toAvatar(AvatarDTO avatarDTO);

    AvatarDTO toAvatarDTO(Avatar avatar);

    List<AvatarDTO> toAvatarDTOs(Collection<Avatar> avatars);
}
