package ru.ac.checkpointmanager.utils;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.*;
import ru.ac.checkpointmanager.model.*;
import ru.ac.checkpointmanager.model.passes.*;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Mapper {

    private static final ModelMapper modelMapper = new ModelMapper();

    /* Checkpoint mapping */
    public static Checkpoint toCheckpoint(CheckpointDTO checkpointDTO) {
        return modelMapper.map(checkpointDTO, Checkpoint.class);
    }

    public static CheckpointDTO toCheckpointDTO(Checkpoint checkpoint) {
        return modelMapper.map(checkpoint, CheckpointDTO.class);
    }

    public static List<CheckpointDTO> toCheckpointsDTO(List<Checkpoint> checkpoints) {
        return checkpoints.stream()
                .map(e -> modelMapper.map(e, CheckpointDTO.class))
                .toList();
    }

    /* Territory mapping */
    public static Territory toTerritory(TerritoryDTO territoryDTO) {
        return modelMapper.map(territoryDTO, Territory.class);
    }

    public static TerritoryDTO toTerritoryDTO(Territory territory) {
        return modelMapper.map(territory, TerritoryDTO.class);
    }

    public static List<TerritoryDTO> toTerritoriesDTO(List<Territory> territories) {
        return territories.stream()
                .map(e -> modelMapper.map(e, TerritoryDTO.class))
                .toList();
    }

    public static List<Territory> toTerritories(List<TerritoryDTO> territoriesDTO) {
        return territoriesDTO.stream()
                .map(e -> modelMapper.map(e, Territory.class))
                .toList();
    }

    /* Pass mapping */
    public static Pass toPass(PassDTO passDTO) {
        if (passDTO.getCar() != null) {
            return modelMapper.map(passDTO, PassAuto.class);
        } else if (passDTO.getPerson() != null) {
            return modelMapper.map(passDTO, PassWalk.class);
        }
        throw new IllegalArgumentException("Ошибка при конвертации passDTO (не содержит car или person)");
    }
  
    public static PassDTO toPassDTO(Pass pass) {
        return modelMapper.map(pass, PassDTO.class);
    }
    public static List<PassDTO> toPassDTO(List<Pass> pass) {
        return pass.stream()
                .map(e -> modelMapper.map(e, PassDTO.class))
                .toList();
    }

    /* User mapping */
    public static User toUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public static UserDTO toUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public static List<UserDTO> toUsersDTO(Collection<User> users) {
        return users.stream()
                .map(e -> modelMapper.map(e, UserDTO.class))
                .toList();
    }

    public static User toUser(UserAuthDTO userAuthDTO) {
        return modelMapper.map(userAuthDTO, User.class);
    }

    public static UserAuthDTO toUserAuthDTO(User user) {
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
    public static User toUser(TemporaryUser temporaryUser) {
        PropertyMap<TemporaryUser, User> propertyMap = new PropertyMap<>() {
            protected void configure() {
                skip(destination.getId());
            }
        };
        modelMapper.addMappings(propertyMap);
        return modelMapper.map(temporaryUser, User.class);
    }

    public static TemporaryUser toTemporaryUser(UserAuthDTO userAuthDTO) {
        return modelMapper.map(userAuthDTO, TemporaryUser.class);
    }

    /* Phone mapping */
    public static Phone toPhone(PhoneDTO phoneDTO) {
        return modelMapper.map(phoneDTO, Phone.class);
    }

    public static PhoneDTO toPhoneDTO(Phone phone) {
        return modelMapper.map(phone, PhoneDTO.class);
    }

    public static List<PhoneDTO> toPhonesDTO(Collection<Phone> phones) {
        return phones.stream()
                .map(p -> modelMapper.map(p, PhoneDTO.class))
                .toList();
    }

    /* Crossing mapping */
    public static Crossing toCrossing(CrossingDTO crossingDTO) {
        return modelMapper.map(crossingDTO, Crossing.class);
    }

    public static CrossingDTO toCrossingDTO(Crossing crossing) {
        return modelMapper.map(crossing, CrossingDTO.class);
    }

    public static List<CrossingDTO> toCrossingsDTO(Collection<Crossing> crossings) {
        return crossings.stream()
                .map(crossing -> modelMapper.map(crossing, CrossingDTO.class))
                .toList();
    }
}
