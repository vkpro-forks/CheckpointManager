package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.user.LoginResponse;
import ru.ac.checkpointmanager.dto.user.UserAuthDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.model.TemporaryUser;
import ru.ac.checkpointmanager.model.User;

import java.util.Collection;
import java.util.List;

@Component
@Slf4j
public class UserMapper {
    private final ModelMapper modelMapper;

    @Autowired
    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        configureModelMapper();
    }

    public User toUser(UserResponseDTO userResponseDTO) {
        return modelMapper.map(userResponseDTO, User.class);
    }

    public UserResponseDTO toUserResponseDTO(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }

    public List<UserResponseDTO> toUserResponseDTOs(Collection<User> users) {
        return users.stream()
                .map(e -> modelMapper.map(e, UserResponseDTO.class))
                .toList();
    }

    public User toUser(UserAuthDTO userAuthDTO) {
        return modelMapper.map(userAuthDTO, User.class);
    }

    public UserAuthDTO toUserAuthDTO(User user) {
        return modelMapper.map(user, UserAuthDTO.class);
    }

    public User toUser(TemporaryUser temporaryUser) {
        return modelMapper.map(temporaryUser, User.class);
    }

    public TemporaryUser toTemporaryUser(User user) {
        return modelMapper.map(user, TemporaryUser.class);
    }

    public TemporaryUser toTemporaryUser(UserAuthDTO userAuthDTO) {
        return modelMapper.map(userAuthDTO, TemporaryUser.class);
    }

    public LoginResponse toLoginResponse(User user) {
        return modelMapper.map(user, LoginResponse.class);
    }

    private void configureModelMapper() {
        PropertyMap<TemporaryUser, User> propertyMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
                skip(destination.getAddedAt());
            }
        };
        modelMapper.addMappings(propertyMap);
    }
}