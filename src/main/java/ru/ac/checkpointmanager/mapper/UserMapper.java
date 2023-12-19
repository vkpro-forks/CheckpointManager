package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.user.ChangeEmailRequest;
import ru.ac.checkpointmanager.dto.user.ConfirmChangeEmail;
import ru.ac.checkpointmanager.dto.user.ConfirmRegistration;
import ru.ac.checkpointmanager.dto.user.LoginResponse;
import ru.ac.checkpointmanager.dto.user.UserAuthDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class UserMapper {
    private final ModelMapper modelMapper;

    @Autowired
    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    public UserResponseDTO toUserResponseDTO(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }

    public List<UserResponseDTO> toUserResponseDTOs(Collection<User> users) {
        return users.stream()
                .map(e -> modelMapper.map(e, UserResponseDTO.class))
                .toList();
    }

    public LoginResponse toLoginResponse(User user) {
        return modelMapper.map(user, LoginResponse.class);
    }

    public ConfirmRegistration toConfirmRegistration(UserAuthDTO userAuthDTO) {
        log.debug("{} was converted to object of ConfirmRegistration.class", userAuthDTO.getEmail());
        return modelMapper.map(userAuthDTO, ConfirmRegistration.class);
    }

    public ConfirmChangeEmail toConfirmChangeEmail(ChangeEmailRequest request) {
        return modelMapper.map(request, ConfirmChangeEmail.class);
    }

    public User toUser(Optional<ConfirmRegistration> confirmRegistration) {
        return modelMapper.map(confirmRegistration, User.class);
    }
}