package ru.ac.checkpointmanager.utils;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.*;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.dto.PassDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.model.Pass;
import ru.ac.checkpointmanager.dto.PhoneDTO;
import ru.ac.checkpointmanager.model.Phone;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;

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

    /* Pass mapping */
    public Pass toPass(PassDTO passDTO) {
        return modelMapper.map(passDTO, Pass.class);
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

    public static User toUser(UserDetailsDTO userDetailsDTO) {
        return modelMapper.map(userDetailsDTO, User.class);
    }

    public static UserDetailsDTO toUserDetailsDTO(User user) {
        return modelMapper.map(user, UserDetailsDTO.class);
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
