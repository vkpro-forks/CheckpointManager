package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.user.ConfirmationRegistrationDTO;
import ru.ac.checkpointmanager.dto.user.NewEmailDTO;
import ru.ac.checkpointmanager.dto.user.ConfirmationEmailDTO;
import ru.ac.checkpointmanager.dto.user.LoginResponseDTO;
import ru.ac.checkpointmanager.dto.user.RegistrationDTO;
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

    public LoginResponseDTO toLoginResponse(User user) {
        return modelMapper.map(user, LoginResponseDTO.class);
    }

    public ConfirmationRegistrationDTO toConfirmRegistration(RegistrationDTO registrationDTO) {
        log.debug("{} was converted to object of ConfirmRegistration.class", registrationDTO.getEmail());
        return modelMapper.map(registrationDTO, ConfirmationRegistrationDTO.class);
    }

    public ConfirmationEmailDTO toConfirmChangeEmail(NewEmailDTO request) {
        return modelMapper.map(request, ConfirmationEmailDTO.class);
    }

    public User toUser(Optional<ConfirmationRegistrationDTO> confirmRegistration) {
        return modelMapper.map(confirmRegistration, User.class);
    }
}