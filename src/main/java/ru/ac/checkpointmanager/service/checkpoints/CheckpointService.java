package ru.ac.checkpointmanager.service.checkpoints;

import ru.ac.checkpointmanager.dto.CheckpointDTO;
import ru.ac.checkpointmanager.dto.CheckpointUpdateDTO;
import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;

import java.util.List;
import java.util.UUID;

public interface CheckpointService {

    CheckpointDTO addCheckpoint(CheckpointDTO checkpointDTO);

    CheckpointDTO findById(UUID id);

    Checkpoint findCheckpointById(UUID id);

    List<CheckpointDTO> findCheckpointsByName(String name);

    List<CheckpointDTO> findAllCheckpoints();

    List<CheckpointDTO> findCheckpointsByTerritoryId(UUID id);

    CheckpointDTO updateCheckpoint(CheckpointUpdateDTO checkpointDTO);

    void deleteCheckpointById(UUID id);
}
