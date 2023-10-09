package ru.ac.checkpointmanager.utils;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.*;
import ru.ac.checkpointmanager.exception.CheckpointNotFoundException;
import ru.ac.checkpointmanager.exception.PassNotFoundException;
import ru.ac.checkpointmanager.model.*;
import ru.ac.checkpointmanager.service.CheckpointService;
import ru.ac.checkpointmanager.service.PassService;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Mapper {



    private final PassService passService;
    private final CheckpointService checkpointService;
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

    /* Phone mapping */
    public Phone toPhone(PhoneDTO phoneDTO) {
        return modelMapper.map(phoneDTO, Phone.class);
    }

    public PhoneDTO toPhoneDTO(Phone phone) {
        return modelMapper.map(phone, PhoneDTO.class);
    }

    public List<PhoneDTO> toPhonesDTO(Collection<Phone> phones) {
        return phones.stream()
                .map(p  -> modelMapper.map(p, PhoneDTO.class))
                .toList();
    }


    /* Crossing mapping */
    public Crossing toCrossing(CrossingDTO crossingDTO) {
        Crossing crossing = new Crossing();
        Pass pass = passService.findPass(crossingDTO.getPassId());
        if (pass == null) {
            throw new PassNotFoundException("Pass not found");
        }

        Checkpoint checkpoint = checkpointService.findCheckpointById(crossingDTO.getCheckpointId());
        if (checkpoint == null) {
            throw new CheckpointNotFoundException("Checkpoint not found");
        }

        crossing.setPass(pass);
        crossing.setCheckpoint(checkpoint);
        crossing.setDirection(crossingDTO.getDirection());

        return crossing;
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
