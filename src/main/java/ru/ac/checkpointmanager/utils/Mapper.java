package ru.ac.checkpointmanager.utils;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.dto.TerritoryDTO;
import ru.ac.checkpointmanager.dto.UserDTO;
import ru.ac.checkpointmanager.model.Checkpoint;
import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;

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

    /* User mapping */
    public User convertToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }
    public List<UserDTO> toUsersDTO(Collection<User> users) {
        return users.stream()
                .map(e -> modelMapper.map(e, UserDTO.class))
                .toList();
    }
}
