package ru.ac.checkpointmanager.service.checkpoints;

import ru.ac.checkpointmanager.model.Checkpoint;

import java.util.List;
import java.util.UUID;

public interface CheckpointService {

    Checkpoint addCheckpoint(Checkpoint checkpoint);

    Checkpoint findCheckpointById(UUID id);

    List<Checkpoint> findCheckpointsByName(String name);

    List<Checkpoint> findAllCheckpoints();

    List<Checkpoint> findCheckpointsByTerritoryId(UUID id);

    Checkpoint updateCheckpoint(Checkpoint checkpoint);

    void deleteCheckpointById(UUID id);
}
