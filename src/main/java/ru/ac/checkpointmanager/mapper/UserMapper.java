package ru.ac.checkpointmanager.mapper;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
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

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }
    public User toUser(UserResponseDTO userResponseDTO) {
        return modelMapper.map(userResponseDTO, User.class);
    }

    public UserResponseDTO toUserDTO(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }

    public List<UserResponseDTO> toUsersDTO(Collection<User> users) {
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

    /**
     * Метод для конвертации временного пользователя в основного.
     * При маппинге игнорируется поле id, так как идентификатор основного пользователя назначается при сохранении в базу данных.
     *
     * @param temporaryUser временный пользователь, который будет конвертирован в основного пользователя.
     * @return User - основной пользователь.
     * @see TemporaryUser
     * @see User
     */
    public User toUser(TemporaryUser temporaryUser) {
        ModelMapper modelMapper = new ModelMapper();
        PropertyMap<TemporaryUser, User> propertyMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
                skip(destination.getAddedAt());
            }
        };
        modelMapper.addMappings(propertyMap);
        try {
            User user = modelMapper.map(temporaryUser, User.class);
            log.info("Конвертация в основного пользователя прошла успешно");
            return user;
        } catch (MappingException e) {
            log.error("Ошибка при конвертации TemporaryUser в User", e);
            throw new RuntimeException("Ошибка при конвертации TemporaryUser в User", e);
        }
    }

    public TemporaryUser toTemporaryUser(User user) {
        ModelMapper modelMapper = new ModelMapper();
        PropertyMap<TemporaryUser, User> propertyMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
                skip(destination.getAddedAt());
            }
        };
        modelMapper.addMappings(propertyMap);
        try {
            TemporaryUser temp = modelMapper.map(user, TemporaryUser.class);
            log.info("Конвертация во временного пользователя прошла успешно");
            return temp;
        } catch (MappingException e) {
            log.error("Ошибка при конвертации User в TemporaryUser", e);
            throw new RuntimeException("Ошибка при конвертации User в TemporaryUser", e);
        }
    }

    public TemporaryUser toTemporaryUser(UserAuthDTO userAuthDTO) {
        return modelMapper.map(userAuthDTO, TemporaryUser.class);
    }

    public LoginResponse toLoginResponse(User user) {
        return modelMapper.map(user, LoginResponse.class);
    }
}
