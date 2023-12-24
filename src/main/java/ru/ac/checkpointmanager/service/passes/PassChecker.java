package ru.ac.checkpointmanager.service.passes;

import java.util.UUID;

public interface PassChecker {

    void checkUserTerritoryRelation(UUID userId, UUID territoryId);

}
