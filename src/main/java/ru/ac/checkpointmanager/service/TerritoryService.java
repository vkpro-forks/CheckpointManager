package ru.ac.checkpointmanager.service;

import ru.ac.checkpointmanager.model.Territory;
import ru.ac.checkpointmanager.model.User;

import java.util.List;
import java.util.UUID;

public interface TerritoryService {

    Territory addTerritory(Territory territory);

    Territory findTerritoryById(UUID id);

    List<User> findUsersByTerritoryId(UUID territoryId);

    List<Territory> findTerritoriesByName(String name);

    List<Territory> findAllTerritories();

    Territory updateTerritory(Territory territory);

    void joinUserToTerritory(UUID territoryId, UUID userId);

    void deleteTerritoryById(UUID id);
}
