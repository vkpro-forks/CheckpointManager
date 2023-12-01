package ru.ac.checkpointmanager.service.checkpoints;

import ru.ac.checkpointmanager.dto.CheckpointDTO;

import java.util.List;
import java.util.UUID;

public interface CheckpointService {

    CheckpointDTO addCheckpoint(CheckpointDTO checkpointDTO);

    CheckpointDTO findById(UUID id);

    List<CheckpointDTO> findCheckpointsByName(String name);

    List<CheckpointDTO> findAllCheckpoints();

    List<CheckpointDTO> findCheckpointsByTerritoryId(UUID id);

    CheckpointDTO updateCheckpoint(CheckpointDTO checkpointDTO);

    void deleteCheckpointById(UUID id);
}
