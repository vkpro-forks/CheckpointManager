package ru.ac.checkpointmanager.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.AvatarDTO;
import ru.ac.checkpointmanager.model.Avatar;

import java.util.Collection;
import java.util.List;

@Component
public class AvatarMapper {

    private final ModelMapper mapper;

    public AvatarMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public Avatar toAvatar(AvatarDTO avatarDTO) {
        return mapper.map(avatarDTO, Avatar.class);
    }

    public AvatarDTO toAvatarDTO(Avatar avatar) {
        return mapper.map(avatar, AvatarDTO.class);
    }

    public List<AvatarDTO> toAvatarDTOs(Collection<Avatar> avatars) {
        return avatars.stream()
                .map(e -> mapper.map(e, AvatarDTO.class))
                .toList();
    }
}
