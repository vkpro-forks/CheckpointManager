package ru.ac.checkpointmanager.utils;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.*;
import ru.ac.checkpointmanager.model.*;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.passes.*;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
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

    /* Pass mapping */
    public Pass toPass(PassDTO passDTO) {
        if (passDTO.getCar() != null) {
            return modelMapper.map(passDTO, PassAuto.class);
        } else if (passDTO.getPerson() != null) {
            return modelMapper.map(passDTO, PassWalk.class);
        }
        throw new IllegalArgumentException("Ошибка при конвертации passDTO (не содержит car или person)");
    }
  
    public PassDTO toPassDTO(Pass pass) {
        return modelMapper.map(pass, PassDTO.class);
    }
    public List<PassDTO> toPassDTO(List<Pass> pass) {
        return pass.stream()
                .map(e -> modelMapper.map(e, PassDTO.class))
                .toList();
    }

    /* User mapping */
    public User toUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public UserDTO toUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public List<UserDTO> toUsersDTO(Collection<User> users) {
        return users.stream()
                .map(e -> modelMapper.map(e, UserDTO.class))
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
     *
     * @see TemporaryUser
     * @see User
     */
    public User toUser(TemporaryUser temporaryUser) {
        PropertyMap<TemporaryUser, User> propertyMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
            }
        };
        modelMapper.addMappings(propertyMap);
        return modelMapper.map(temporaryUser, User.class);
    }

    public TemporaryUser toTemporaryUser(UserAuthDTO userAuthDTO) {
        return modelMapper.map(userAuthDTO, TemporaryUser.class);
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

    /* Crossing mapping */
    public Crossing toCrossing(CrossingDTO crossingDTO) {
        return modelMapper.map(crossingDTO, Crossing.class);
    }

    public CrossingDTO toCrossingDTO(Crossing crossing) {
        return modelMapper.map(crossing, CrossingDTO.class);
    }

    public List<CrossingDTO> toCrossingsDTO(Collection<Crossing> crossings) {
        return crossings.stream()
                .map(crossing -> modelMapper.map(crossing, CrossingDTO.class))
                .toList();
    }
}
