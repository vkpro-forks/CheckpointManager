package ru.ac.checkpointmanager.mapper.avatar;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.ac.checkpointmanager.dto.avatar.AvatarDTO;
import ru.ac.checkpointmanager.model.avatar.Avatar;

import java.util.Collection;
import java.util.List;
@Service
public class AvatarMapperImpl implements AvatarMapper {

    private final ModelMapper mapper;

    @Autowired
    public AvatarMapperImpl(ModelMapper mapper) {
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
