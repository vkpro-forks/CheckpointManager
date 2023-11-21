package ru.ac.checkpointmanager.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.*;
import ru.ac.checkpointmanager.dto.user.LoginResponse;
import ru.ac.checkpointmanager.dto.user.UserAuthDTO;
import ru.ac.checkpointmanager.dto.user.UserResponseDTO;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.model.*;
import ru.ac.checkpointmanager.model.car.Car;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.passes.Pass;
import ru.ac.checkpointmanager.repository.CheckpointRepository;
import ru.ac.checkpointmanager.repository.PassRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class Mapper {

    private final ModelMapper modelMapper = new ModelMapper();

    /* Checkpoint mapping */
    public Checkpoint toCheckpoint(CheckpointDTO checkpointDTO) {
        return modelMapper.map(checkpointDTO, Checkpoint.class);
    }

    public CheckpointDTO toCheckpointDTO(Checkpoint checkpoint) {
        return modelMapper.map(checkpoint, CheckpointDTO.class);
    }

    public List<CheckpointDTO> toCheckpointsDTO(List<Checkpoint> checkpoints) {
        return checkpoints.stream()
                .map(e -> modelMapper.map(e, CheckpointDTO.class))
                .toList();
    }

    /* Territory mapping */
    public Territory toTerritory(TerritoryDTO territoryDTO) {
        return modelMapper.map(territoryDTO, Territory.class);
    }

    public TerritoryDTO toTerritoryDTO(Territory territory) {
        return modelMapper.map(territory, TerritoryDTO.class);
    }

    public List<TerritoryDTO> toTerritoriesDTO(List<Territory> territories) {
        return territories.stream()
                .map(e -> modelMapper.map(e, TerritoryDTO.class))
                .toList();
    }

    public List<Territory> toTerritories(List<TerritoryDTO> territoriesDTO) {
        return territoriesDTO.stream()
                .map(e -> modelMapper.map(e, Territory.class))
                .toList();
    }

    /* User mapping */
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

    /* Phone mapping */
    public Phone toPhone(PhoneDTO phoneDTO) {
        return modelMapper.map(phoneDTO, Phone.class);
    }

    public PhoneDTO toPhoneDTO(Phone phone) {
        return modelMapper.map(phone, PhoneDTO.class);
    }

    public List<PhoneDTO> toPhonesDTO(Collection<Phone> phones) {
        return phones.stream()
                .map(p -> modelMapper.map(p, PhoneDTO.class))
                .toList();
    }

}
