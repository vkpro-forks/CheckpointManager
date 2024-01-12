package ru.ac.checkpointmanager.service.passes;

import ru.ac.checkpointmanager.model.checkpoints.Checkpoint;
import ru.ac.checkpointmanager.model.passes.Pass;

import java.util.UUID;

public interface PassChecker {

    void checkUserTerritoryRelation(UUID userId, UUID territoryId);

    void checkPassActivity(Pass pass);

    void checkPassAndCheckpointTerritories(Pass pass, Checkpoint checkpoint);

    void checkPassAndCheckpointCompatibility(Pass pass, Checkpoint checkpoint);

}
